import styled from 'styled-components';

export const DetailBody = styled.div``;

export const DetailMain = styled.main`
  background-color: var(--backcolor);
  padding-top: 60px;
  width: 100%;
  height: 800px;
  display: flex;
  align-items: center;
  justify-content: center;
  .DetailBox {
    border-radius: 10px;
    margin-top: 50px;
    width: 600px;
    height: 650px;
    padding: 50px 50px 0px;
    background-color: #fff;
    display: flex;
    flex-direction: column;
    .contentTitle {
      margin-top: 30px;
      margin-bottom: 5px;
    }
    .contentinput {
      border: none;
      padding: 10px;
      height: 200px;
      box-shadow: inset var(--box-shadow);
      outline: none;
      border-radius: 5px;
      &::placeholder {
        color: var(--black-200);
        font-weight: bold;
      }
    }
    .tagTitle {
      margin-top: 20px;
      margin-bottom: 5px;
    }
    .taginput {
      padding: 5px;
      height: 30px;
      border: none;
      box-shadow: inset var(--box-shadow);
      border-radius: 5px;
      outline: none;
      &::placeholder {
        color: var(--black-200);
        font-weight: bold;
      }
    }
    > .buttons {
      display: flex;
      align-items: flex-end;
      justify-content: center;
      height: 100px;
      > button {
        cursor: pointer;
        width: 120px;
        height: 50px;
        font-size: var(--font-18);
        font-weight: 700;
        margin: 0 20px;
        color: #fff;
        border: none;
        border-radius: 20px;
        box-shadow: var(--box-shadow);
      }
    }
    .submitbutton {
      background-color: var(--logored);
    }
    .canclebutton {
      background-color: var(--buttongray);
    }
  }
  .Imgbox {
    display: flex;
    position: relative;
    justify-content: center;
    .Imgaddbox {
      border: 3px solid var(--black-200);
      border-radius: 3px;
      width: 110px;
      height: 150px;
      display: flex;
      align-items: center;
      justify-content: center;
      .ImgInput {
        display: none;
      }
      .ImgButton {
        cursor: pointer;
        padding: 50px 25px;
        border: none;
        background-color: transparent;
      }
    }
    .boxs {
      position: relative;
      .Imgs {
        width: 110px;
        height: 150px;
        margin: 0 10px;
        border-radius: 3px;
      }
      .Imgdel {
        position: absolute;
        opacity: 0.8;
        border: none;
        font-size: var(--font-18);
        right: 10px;
        cursor: pointer;
      }
    }
  }
`;
