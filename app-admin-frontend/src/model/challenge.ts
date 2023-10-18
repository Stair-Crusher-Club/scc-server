import {AdminChallengeActionConditionTypeEnumDTO} from "../api";
import {MultiSelectOption} from "../component/MultiSelect";


export const displayNameByAdminChallengeActionConditionType = {
    'BUILDING_ACCESSIBILITY': '건물 정보 등록',
    'BUILDING_ACCESSIBILITY_COMMENT': '건물 코멘트 등록',
    'PLACE_ACCESSIBILITY': '장소 정보 등록',
    'PLACE_ACCESSIBILITY_COMMENT': '장소 코멘트 등록',
}

export const adminChallengeActionConditionTypeOptions: MultiSelectOption<AdminChallengeActionConditionTypeEnumDTO>[] = Object.values(AdminChallengeActionConditionTypeEnumDTO).map(type => {
    if (displayNameByAdminChallengeActionConditionType[type] == null) {
        throw Error(`displayName for type ${type} does not exists.`);
    }
    return {
        value: type,
        displayName: displayNameByAdminChallengeActionConditionType[type],
    }
});
