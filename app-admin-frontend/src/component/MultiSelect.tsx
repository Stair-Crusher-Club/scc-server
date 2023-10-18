import {MenuItem} from "@blueprintjs/core";
import * as React from "react";
import {MouseEventHandler} from "react";

export interface MultiSelectOption<T> {
    displayName: string;
    value: T;
}

export interface MultiSelectItemProps {
    onClick: MouseEventHandler;
    option: MultiSelectOption<any>;
}

export function MultiSelectItem(props: MultiSelectItemProps) {
    return (
        <MenuItem
            roleStructure="listoption"
            shouldDismissPopover={false}
            onClick={props.onClick}
            text={props.option.displayName}
        />
    )
}