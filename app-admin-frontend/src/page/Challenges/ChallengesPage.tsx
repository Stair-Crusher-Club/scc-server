import { Button, ButtonGroup } from '@blueprintjs/core';
import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AdminApis } from '../../AdminApi';
import { AdminChallengeDTO } from '../../api';

import './ChallengesPage.scss';

function ChallengesPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [challenges, setChallenges] = useState<AdminChallengeDTO[]>([]);
  const navigate = useNavigate();

  function withLoading(promise: Promise<any>): Promise<any> {
    setIsLoading(true);
    return promise.finally(() => setIsLoading(false));
  }

  useEffect(() => {
    withLoading(
      AdminApis.challenge.challengesGet()
        .then(res => setChallenges(res.data) )
    );
  }, []);

  function onChallengeClick(challenge: AdminChallengeDTO) {
    return () => {
      navigate(`/challenges/${challenge.id}`);
    };
  }

  function onChallengeDeleteBtnClick(challenge: AdminChallengeDTO) {
    return async (e: React.MouseEvent) => {
      e.stopPropagation();
      if (!window.confirm(`정말 ${challenge.name} 챌린지를 삭제하시겠습니까?`)) {
        return;
      }
      await withLoading(
        AdminApis.challenge.challengesChallengeIdDelete(challenge.id)
      );
      alert('삭제를 완료했습니다.');
      
      const res = await AdminApis.challenge.challengesGet()
      setChallenges(res.data);
    };
  }

  return (
    <div>
      <h1>챌린지</h1>
      <ButtonGroup alignText="right">
        <Link to="/challenges/create">
          <Button text="새 챌린지 생성" />
        </Link>
      </ButtonGroup>
      <table className="challenges bp4-html-table bp4-html-table-bordered bp4-html-table-condensed bp4-interactive">
        <thead>
          <tr>
            <th className="title-column">챌린지 이름</th>
            <th>삭제</th>
          </tr>
        </thead>
        <tbody>
          {challenges.map((challenge) => {
            return (
              <tr onClick={onChallengeClick(challenge)}>
                <td>{challenge.name}</td>
                <td><Button icon="trash" disabled={isLoading} onClick={onChallengeDeleteBtnClick(challenge)} /></td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  );
}

export default ChallengesPage;
