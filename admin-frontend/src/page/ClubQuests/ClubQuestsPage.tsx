import { Button, ButtonGroup } from '@blueprintjs/core';
import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AdminApi } from '../../AdminApi';
import { ClubQuestsGet200ResponseInner } from '../../api';

import './ClubQuestsPage.scss';

function ClubQuestsPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [clubQuests, setClubQuests] = useState<ClubQuestsGet200ResponseInner[]>([]);
  const navigate = useNavigate();

  function withLoading(promise: Promise<any>): Promise<any> {
    setIsLoading(true);
    return promise.finally(() => setIsLoading(false));
  }

  useEffect(() => {
    withLoading(
      AdminApi.clubQuestsGet()
        .then(res => setClubQuests(res.data) )
    );
  }, []);

  function onClubQuestClick(clubQuest: ClubQuestsGet200ResponseInner) {
    return () => {
      navigate(`/clubQuests/${clubQuest.id}`);
    };
  }

  function onClubQuestDeleteBtnClick(clubQuest: ClubQuestsGet200ResponseInner) {
    return (e: React.MouseEvent) => {
      e.stopPropagation();
      if (!window.confirm(`정말 ${clubQuest.name} 퀘스트를 삭제하시겠습니까?`)) {
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
      <table className="club-quests bp4-html-table bp4-html-table-bordered bp4-html-table-condensed bp4-interactive">
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
                <td>{clubQuest.name}</td>
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
