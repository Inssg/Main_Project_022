package com.backend.fitchallenge.domain.calendar.service;

import com.backend.fitchallenge.domain.calendar.dto.request.RecordCreate;
import com.backend.fitchallenge.domain.calendar.dto.response.*;
import com.backend.fitchallenge.domain.calendar.exception.*;
import com.backend.fitchallenge.domain.calendar.repository.QueryRecordSportsRepository;
import com.backend.fitchallenge.domain.calendar.util.CalendarId;
import com.backend.fitchallenge.domain.challenge.repository.ChallengeRepository;
import com.backend.fitchallenge.domain.member.entity.Member;
import com.backend.fitchallenge.domain.member.exception.MemberNotExist;
import com.backend.fitchallenge.domain.member.repository.MemberRepository;
import com.backend.fitchallenge.domain.calendar.dto.request.RecordUpdate;
import com.backend.fitchallenge.domain.calendar.entity.Record;
import com.backend.fitchallenge.domain.calendar.entity.Sports;
import com.backend.fitchallenge.domain.calendar.repository.CalendarRepository;
import com.backend.fitchallenge.domain.calendar.repository.RecordRepository;
import com.backend.fitchallenge.domain.post.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.backend.fitchallenge.domain.post.entity.QPost.post;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecordService {

    private final RecordRepository recordRepository;
    private final CalendarRepository calendarRepository;
    private final MemberRepository memberRepository;
    private final QueryRecordSportsRepository recordSportsRepository;
    private final ChallengeRepository challengeRepository;
    private final SportsService sportsService;
    private final AwsS3Service awsS3Service;

    /**
     * [운동 기록 등록]
     * 1. 입력한 날짜가 db 캘린더의 범위에 있는지 확인합니다.
     * 2. memberId와 날짜가 중복된 등록 요청이 아닌지 확인합니다.
     *    (1, 2는 등록 가능한 날짜의 달력만 보여주고, 첫 요청 이후에는 수정만 가능하게 하는 등
     *    프론트엔드 단에서 구조적으로 막을 수 있는 부분이지만 혹시 몰라서 일단 넣어놨습니다.
     *    시험을 하실 때 불편하시다면 해당 부분만 주석처리하셔서 실행해주시길 바라며, 추후 삭제 가능합니다.)
     * 3. 데이터베이스에 id가 존재하지 않는 sports를 담은 요청이 있는지 확인합니다.
     *    여기서 확인해주어야 RecordCreate의 sportsRequest의 리스트와
     *    이를 기반으로 db에서 가져오는 sports의 리스트가 동기화되어 추후 로직에 영향을 주지 않습니다.
     *
     * 4. record에 입력할 sports의 목록을 생성합니다.
     * 5. record를 생성합니다.
     */
    // todo : 캘린더 존재 여부 확인하는 과정 필요한지 논의 필요
    public Long createRecord(Long memberId, RecordCreate recordCreate) {

        //1에 해당하는 부분
        if (!calendarRepository.existsById(CalendarId.of(recordCreate))) {
            throw new CalendarNotFound();
        }

        //2에 해당하는 부분
        if (recordRepository.exist(memberId, recordCreate.getStart()))
            throw new DuplicateRecordCreation();

        List<Sports> sports = sportsService.getSports(recordCreate.getSports());

        Record record = Record.createRecord(recordCreate, memberId, sports);

        return recordRepository.save(record).getId();
    }

    /**
     * [특정 유저의 월간 운동 기록 조회]
     * 1. memberId와 월을 사용해 record가 존재하는지 확인하고, 존재한다면 record의 목록을 반환합니다.
     * 2. record 목록과 각각의 memberResponse를 recordResponse로 변환하고 반환합니다.
     */
    // todo : db 접근 효율성 고려해 조회 방법 선택
    @Transactional(readOnly = true)
    public SimpleRecordResponse getMonthlyRecordList(Long memberId, int month) {
        log.info("memberId: " + memberId + ", month: " + month);
        Member findMember = memberRepository.findById(memberId).orElseThrow(MemberNotExist::new);
        Member opponent = memberRepository.findOpponent(memberId);

        List<PersonalSimpleRecordResponse> recordResponses = recordRepository.findByMemberIdAndMonth(memberId, month).stream()
                .map(record -> PersonalSimpleRecordResponse.builder()
                        .recordId(record.getId())
                        .timeRecord(Duration.between(record.getStartTime(), record.getEndTime()))
                        .date(LocalDate.of(record.getYear(), record.getMonth(), record.getDay()))
                        .volume(record.getVolume())
                        .result(record.getResult().toString())
                        .build()
                ).collect(toList());

        //챌린지 상대가 없는 경우
        if (opponent == null) {

            return SimpleRecordResponse.containingOnly(recordResponses);
        }

        //챌린지 상대가 있는 경우
        else {
            List<PersonalSimpleRecordResponse> opRecordResponses =
                    recordRepository.findByMemberIdAndOpponentId(findMember.getId(), opponent.getId())
                            .stream()
                            .map(record -> PersonalSimpleRecordResponse.builder()
                                    .recordId(record.getId())
                                    .timeRecord(Duration.between(record.getStartTime(), record.getEndTime()))
                                    .date(LocalDate.of(record.getYear(), record.getMonth(), record.getDay()))
                                    .volume(record.getVolume())
                                    .result(record.getResult().toString())
                                    .build()
                            ).collect(toList());

            return SimpleRecordResponse.containingBoth(recordResponses, opRecordResponses);
        }
    }

    /**
     * [운동 기록 수정]
     * 1. 날짜(연, 월, 일)와 요청을 보낸 memberId를 통해 record가 존재하는지 확인하고, 존재한다면 해당 record를 반환합니다.
     * 2. record에 입력할 sports의 목록을 생성합니다.
     * 3. sports, startTime, endTime 등의 수정사항을 record에 반영합니다.
     */
    // todo : 추후 비즈니스 로직 추가되면 반영
    public Long updateRecord(Long memberId, Long recordId, RecordUpdate recordUpdate) {

        Record findRecord = recordRepository
                .findById(recordId)
                .orElseThrow(RecordNotFound::new);
        verifyWriter(memberId, findRecord);

        List<Sports> sports = sportsService.getSports(recordUpdate.getSports());

        findRecord.updateRecord(recordUpdate, sports);

        return findRecord.getId();
    }

    /**
     * [운동 기록 삭제]
     * 1. 요청을 보낸 member가 존재하는지 확인합니다.
     * 2. recordId를 통해 record가 존재하는지 확인하고 해당 record를 반환합니다.
     * 3. 요청을 보낸 Member가 record의 writer인지 확인합니다.
     * 4. 맞다면, record를 삭제합니다.
     */
    public Long deleteRecord(Long memberId, Long recordId) {

        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotExist::new);
        Record findRecord = findVerifiedRecord(recordId);
        verifyWriter(member.getId(), findRecord);

        List<String> files = findRecord.getTimePicture().toPathList();

        //s3에서 record에 해당하는 start와 end 사진 삭제
        awsS3Service.DeleteFile(files);

        recordRepository.delete(findRecord);

        return recordId;
    }

    // record를 조회하고, 존재하면 해당 객체를 반환합니다.
    @Transactional(readOnly = true)
    private Record findVerifiedRecord(Long recordId) {
        Optional<Record> optionalRecord = recordRepository.findById(recordId);

        return optionalRecord.orElseThrow(RecordNotFound::new);
    }

    // 어떤 member가 record의 writer인지 확인합니다.
    @Transactional(readOnly = true)
    private void verifyWriter(Long memberId, Record findRecord) {

        Long writerId = recordRepository.findMemberIdByRecordId(findRecord.getId());

        if (!writerId.equals(memberId)) {
            throw new NotRecordWriter();
        }
    }

    //기존 DB에서 하나하나 조회하는 방식이 아니라 운동 ID의 범주에 속하는지를 확인하여
    //DB에 등록된 운동이 변동할 때마다 손수 바꾸어줘야 한다는 점이 걸려서 주석처리해놨습니다.

    //sportsRequest의 목록을 순회하며 sportsId를 기준으로 db에 등록된 운동의 범위 내에 존재하는지 확인합니다.
//    private void verifySportsRequests(RecordCreate recordCreate) {
//        recordCreate.getSports().stream().forEach(sportsRequest -> {
//            Long sportsId = sportsRequest.getSportsId();
//            if (sportsId < 1 || sportsId > 66)
//                throw new SportsNotFound();
//        });
//    }
}
