import {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {Button, Checkbox, InputGroup, NumericInput, Tag} from '@blueprintjs/core';
import {AdminApis} from '../../AdminApi';

import './CreateChallengePage.scss';
import {DatePicker, TimePrecision} from "@blueprintjs/datetime";
import {
  AdminChallengeActionConditionTypeEnumDTO,
  AdminChallengeConditionDTO,
  AdminCreateChallengeRequestDTO
} from "../../api";
import {formatDate} from "../../util/date";
import {adminChallengeActionConditionTypeOptions} from "../../model/challenge";
import {MultiSelect2} from "@blueprintjs/select";
import {MultiSelectItem, MultiSelectOption} from "../../component/MultiSelect";

declare global {
  interface Window {
    kakao: any;
  }
}

function CreateChallengePage() {
  const [isLoading, setIsLoading] = useState(false);
  const [challengeName, setChallengeName] = useState('');
  const [isPublic, setIsPublic] = useState(true);
  const [passcode, setPasscode] = useState<string | undefined>(undefined);
  const [invitationCode, setInvitationCode] = useState<string | undefined>(undefined);
  const [startsAtDate, setStartsAtDate] = useState<Date>(new Date());
  const [endsAtDate, _setEndsAtDate] = useState<Date | null>(null);
  const setEndsAtDate = (value: Date | null) => {
    if (value != null) {
      if (startsAtDate >= value) {
        alert('종료 시각은 시작 시각 이후로 설정되어야 합니다.');
        return;
      }
    }
    _setEndsAtDate(value);
  }
  const [goal, setGoal] = useState<number>(1);
  const [milestonesText, setMilestonesText] = useState<string>('');
  const getMilestones = (): number[] => {
    if (milestonesText.trim() == '') {
      return [];
    }
    return milestonesText.split(',').map(it => Number(it.trim())).sort((n1,n2) => n1 - n2);
  }
  const [conditionEupMyeonDongsText, setConditionEupMyeonDongsText] = useState<string>('');
  const [conditionActionTypeOptions, setConditionActionTypeOptions] = useState<MultiSelectOption<AdminChallengeActionConditionTypeEnumDTO>[]>(adminChallengeActionConditionTypeOptions);
  const getConditions = (): AdminChallengeConditionDTO[] => {
    const conditionEupMyeonDongs = conditionEupMyeonDongsText.split(',').map(it => it.trim()).filter(it => it)
    return [{
      addressCondition: {
        rawEupMyeonDongs: conditionEupMyeonDongs,
      },
      actionCondition: {
        types: conditionActionTypeOptions.length > 0 ? conditionActionTypeOptions.map(it => it.value) : undefined,
      },
    }];
  }

  const navigate = useNavigate();

  function withLoading(promise: Promise<any>): Promise<any> {
    setIsLoading(true);
    return promise.finally(() => setIsLoading(false));
  }

  const getCreateChallengeRequestDTO = (): AdminCreateChallengeRequestDTO => {
    return {
      name: challengeName,
      isPublic,
      invitationCode: isPublic ? undefined : invitationCode,
      passcode: passcode == null || passcode == '' ? undefined : passcode!.trim(),
      startsAtMillis: startsAtDate.getTime(),
      endsAtMillis: endsAtDate?.getTime(),
      goal,
      milestones: getMilestones(),
      conditions: getConditions(),
    }
  }

  async function createChallenge() {
    if (!window.confirm('챌린지를 생성하시겠습니까?')) {
      return;
    }
    withLoading((
      async () => {
        await AdminApis.challenge.challengesPost(getCreateChallengeRequestDTO());
        alert('챌린지 생성을 완료했습니다.');
        navigate('/challenges');
      }
    )());
  }

  const isFormValid = () => {
    const request = getCreateChallengeRequestDTO();
    return (
        request.name != ''
        && (isPublic || (request.invitationCode != null && request.invitationCode != ''))
        && goal > 0
        && (getMilestones().length === 0 || getMilestones()[getMilestones().length - 1] < goal)
    );
  }

  const onSelectConditionActionTypeOptions = (option: MultiSelectOption<AdminChallengeActionConditionTypeEnumDTO>) => {
    if (conditionActionTypeOptions.some(it => it.value === option.value)) {
      removeConditionActionTypeOption(option);
      return;
    }
    setConditionActionTypeOptions([...conditionActionTypeOptions, option]);
  }
  const removeConditionActionTypeOption = (option: MultiSelectOption<AdminChallengeActionConditionTypeEnumDTO>) => {
    setConditionActionTypeOptions(conditionActionTypeOptions.filter(it => it.value !== option.value));
  }

  return (
    <div>
      <h1>챌린지 생성하기</h1>
      <div className="create-challenge-page-body">
        <div>
          <div className="input-group">
            <span>챌린지 이름 :&nbsp;</span>
            <InputGroup
              className="inline-flex"
              value={challengeName}
              onChange={(event) => { setChallengeName(event.target.value); }}
              disabled={isLoading}
            />
          </div>
          <div>
            <span>참여코드 :&nbsp;</span>
            <InputGroup
              className="input-group"
              value={passcode}
              onChange={(event) => { setPasscode(event.target.value); }}
              disabled={isLoading}
            />
          </div>
          <div>
            <span>공개 여부 :&nbsp;</span>
            <Checkbox
              className="input-group"
              checked={isPublic}
              onChange={(event) => { setIsPublic((event.target as HTMLInputElement).checked); }}
              disabled={isLoading}
            />
            <span>초대 코드 :&nbsp;</span>
            <InputGroup
              className="input-group"
              value={invitationCode}
              onChange={(event) => { setInvitationCode(event.target.value); }}
              disabled={isLoading || isPublic}
            />
            {
              !isPublic && (invitationCode == null || invitationCode == '')
                ? (
                  <span style={{'color': 'red'}}>비공개 챌린지는 초대 코드를 지정해야 합니다.</span>
                )
                : null
            }
          </div>
          <div>
            <div className="input-group">
              <p>
                시작 시각 :&nbsp;
                {
                  startsAtDate == null
                    ? <Tag minimal={true}>없음</Tag>
                    : <Tag intent="primary">{formatDate(startsAtDate)}</Tag>
                }
              </p>
              <DatePicker
                timePrecision={TimePrecision.MINUTE}
                highlightCurrentDay={true}
                canClearSelection={false}
                value={startsAtDate}
                onChange={(newDate) => { setStartsAtDate(newDate); }}
              />
            </div>
            <div className="input-group">
              <p>
                종료 시각 :&nbsp;
                {
                  endsAtDate == null
                    ? <Tag minimal={true}>없음</Tag>
                    : <Tag intent="primary">{formatDate(endsAtDate)}</Tag>
                }
              </p>
              <DatePicker
                timePrecision={TimePrecision.MINUTE}
                highlightCurrentDay={true}
                canClearSelection={true}
                value={endsAtDate}
                onChange={(newDate) => { setEndsAtDate(newDate); }}
              />
            </div>
          </div>
          <div>
            <span>목표 :&nbsp;</span>
            <div className="input-group">
              <NumericInput
                value={goal}
                onValueChange={(value) => { setGoal(value); }}
                disabled={isLoading}
                min={1}
              />
            </div>
            <span>마일스톤(쉼표로 구분) :&nbsp;</span>
            <InputGroup
              className="input-group"
              value={milestonesText}
              onChange={(event) => { setMilestonesText(event.target.value); }}
              disabled={isLoading}
            />
            {

              getMilestones()[getMilestones().length - 1] >= goal
                ? (
                  <span style={{'color': 'red'}}>목표는 마일스톤보다 커야 합니다.</span>
                )
                : null
            }
          </div>
          <div>
            <span>퀘스트 대상 지역 읍면동(쉼표로 구분하며, 지정하지 않으면 모든 지역이 대상이 됩니다) :&nbsp;</span>
            <InputGroup
                className="input-group"
                value={conditionEupMyeonDongsText}
                onChange={(event) => { setConditionEupMyeonDongsText(event.target.value); }}
                placeholder="전체 지역"
                disabled={isLoading}
            />
          </div>
          <div>
            <span>퀘스트 대상 액션 :&nbsp;</span>
            <MultiSelect2
                className="input-group"
                items={adminChallengeActionConditionTypeOptions}
                selectedItems={conditionActionTypeOptions}
                itemRenderer={(option, props) => <MultiSelectItem onClick={props.handleClick} option={option} />}
                onItemSelect={(option) => onSelectConditionActionTypeOptions(option)}
                onRemove={(option, _) => removeConditionActionTypeOption(option)}
                onClear={() => setConditionActionTypeOptions([])}
                tagRenderer={(option) => option.displayName}
                placeholder="전체 액션"
                disabled={isLoading}
            />
          </div>
        </div>
        <div className="input-group">
          <Button icon="confirm" text="챌린지 생성" onClick={createChallenge} disabled={isLoading || !isFormValid()}></Button>
        </div>
      </div>
    </div>
  );
}

export default CreateChallengePage;
