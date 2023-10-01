import {useEffect, useState} from 'react';
import {useParams} from 'react-router-dom';
import {Checkbox, InputGroup, NumericInput, Tag} from '@blueprintjs/core';
import {AdminApis} from '../../AdminApi';

import './ChallengePage.scss';
import {DatePicker, TimePrecision} from "@blueprintjs/datetime";
import {AdminChallengeDTO} from "../../api";
import {formatDate} from "../../util/date";

declare global {
  interface Window {
    kakao: any;
  }
}

function ChallengePage() {
  const [isLoading, setIsLoading] = useState(false);
  const [challenge, setChallenge] = useState<AdminChallengeDTO>();

  function withLoading(promise: Promise<any>): Promise<any> {
    setIsLoading(true);
    return promise.finally(() => setIsLoading(false));
  }

  const { id: _rawChallengeId } = useParams();
  const challengeId = _rawChallengeId!
  useEffect(() => {
    withLoading(
      AdminApis.challenge.challengesChallengeIdGet(challengeId)
        .then((res) => {
          const challenge = res.data;
          setChallenge(challenge);
        })
    );
  }, []);

  return (
    challenge == null
      ? <div />
      : (
        <div>
          <h1>챌린지 {challenge!.name}</h1>
          <div className="create-challenge-page-body">
            <div>
              <div>
                <span>패스코드 :&nbsp;</span>
                <InputGroup
                  className="input-group"
                  value={challenge!.passcode}
                  disabled={true}
                />
              </div>
              <div>
                <span>공개 여부 :&nbsp;</span>
                <Checkbox
                  className="input-group"
                  checked={challenge!.isPublic}
                  disabled={true}
                />
                <span>초대 코드 :&nbsp;</span>
                <InputGroup
                  className="input-group"
                  value={challenge!.invitationCode}
                  disabled={true}
                />
              </div>
              <div>
                <div className="input-group">
                  <p>
                    시작 시각 :&nbsp;
                    {
                      challenge!.startsAtMillis == null
                        ? <Tag minimal={true}>없음</Tag>
                        : <Tag intent="primary">{formatDate(new Date(challenge!.startsAtMillis))}</Tag>
                    }
                  </p>
                  <DatePicker
                    timePrecision={TimePrecision.MINUTE}
                    highlightCurrentDay={true}
                    canClearSelection={false}
                    value={new Date(challenge!.startsAtMillis)}
                  />
                </div>
                <div className="input-group">
                  <p>
                    종료 시각 :&nbsp;
                    {
                      challenge!.endsAtMillis == null
                        ? <Tag minimal={true}>없음</Tag>
                        : <Tag intent="primary">{formatDate(new Date(challenge!.endsAtMillis))}</Tag>
                    }
                  </p>
                  <DatePicker
                    timePrecision={TimePrecision.MINUTE}
                    highlightCurrentDay={true}
                    canClearSelection={true}
                    value={challenge!.endsAtMillis ? new Date(challenge!.endsAtMillis!) : undefined}
                  />
                </div>
              </div>
              <div>
                <span>목표 :&nbsp;</span>
                <div className="input-group">
                  <NumericInput
                    value={challenge!.goal}
                    disabled={true}
                  />
                </div>
                <span>마일스톤(쉼표로 구분) :&nbsp;</span>
                <InputGroup
                  className="input-group"
                  value={challenge!.milestones.join(",")}
                  disabled={true}
                />
              </div>
            </div>
          </div>
        </div>
      )
  );
}

export default ChallengePage;