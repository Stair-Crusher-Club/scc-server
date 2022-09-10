import { AnchorButton, Button, ButtonGroup } from '@blueprintjs/core';
import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { ClubQuestDTO } from '../../type';

import './ClubQuestsPage.scss';

function ClubQuestsPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [clubQuests, setClubQuests] = useState<ClubQuestDTO[]>([]);

  // TODO: 페이지 접근 시 퀘스트 목록 로딩

  function onClubQuestClick(clubQuest: ClubQuestDTO) {
    return () => { /* TODO: 퀘스트 상세창으로 이동 */ };
  }

  function onClubQuestDeleteBtnClick(clubQuest: ClubQuestDTO) {
    return (e: React.MouseEvent) => {
      e.stopPropagation();
      if (!window.confirm(`정말 ${clubQuest.title} 퀘스트를 삭제하시겠습니까?`)) {
        return;
      }
      // TODO: 퀘스트 삭제
    };
  }

  return (
    <div>
      <h1>퀘스트</h1>
      <ButtonGroup alignText="right">
        <Link to="/clubQuest/create">
          <Button text="새 퀘스트 생성" />
        </Link>
      </ButtonGroup>
      <table className="club-quests bp3-html-table bp3-html-table-bordered bp3-html-table-condensed bp3-interactive">
        <thead>
          <tr>
            <th className="title-column">퀘스트 이름</th>
            <th>삭제</th>
          </tr>
        </thead>
        <tbody>
          {clubQuests.map((clubQuest) => {
            return (
              <tr onClick={onClubQuestClick(clubQuest)}>
                <td>{clubQuest.title}</td>
                <td><Button icon="trash" disabled={isLoading} onClick={onClubQuestDeleteBtnClick(clubQuest)} /></td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  );
}

export default ClubQuestsPage;
