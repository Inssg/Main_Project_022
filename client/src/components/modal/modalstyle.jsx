import styled from 'styled-components';

export const Wrapper = styled.div`
  padding-right: 10px;
  .modal {
    display: none;
    position: fixed;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 99;
    background-color: rgba(0, 0, 0, 0.6);
  }
  .modal.openModal {
    display: flex;
    align-items: center;
    /* 팝업이 열릴때 스르륵 열리는 효과 */
    animation: modal-bg-show 0.3s;
  }
  @keyframes modal-show {
    from {
      opacity: 0;
      margin-top: -50px;
    }
    to {
      opacity: 1;
      margin-top: 0;
    }
  }
  @keyframes modal-bg-show {
    from {
      opacity: 0;
    }
    to {
      opacity: 1;
    }
  }
`;

export const ModalBtn = styled.div`
  outline: none;
  cursor: pointer;
  border: 0;
`;

export const ModalSection = styled.section`
  width: 90%;
  max-width: 450px;
  margin: 0 auto;
  border-radius: 0.3rem;
  background-color: #fff;
  /* 팝업이 열릴때 스르륵 열리는 효과 */
  animation: modal-show 0.3s;
  overflow: hidden;
`;

export const ModalHeader = styled.section`
  position: relative;
  padding: 16px 0px;
  background-color: #f1f1f1;
  font-weight: 700;
  text-align: center;
  font-size: var(--font-21);
`;

export const ModalHeaderBtn = styled.section`
  position: absolute;
  top: 15px;
  right: 15px;
  width: 30px;
  font-size: 21px;
  font-weight: 700;
  text-align: center;
  color: #999;
  background-color: transparent;
  :hover {
    cursor: pointer;
    color: black;
  }
`;
export const ModalMain = styled.main`
  padding: 16px;
  border-bottom: 1px solid #dee2e6;
  border-top: 1px solid #dee2e6;
`;

export const ModalList = styled.li`
  display: flex;
  align-items: center;
  list-style: none;
  padding: 10px;
  > img {
    margin-right: 10px;
  }
  .content {
    width: 100%;
    .fightday {
      display: flex;
      justify-content: space-between;
    }
    > div {
      margin-top: 10px;
    }
  }
`;