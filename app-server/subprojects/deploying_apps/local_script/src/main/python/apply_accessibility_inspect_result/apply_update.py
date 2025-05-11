import csv
import json
import requests
from dotenv import load_dotenv
import os

load_dotenv()

ACCESS_TOKEN = os.getenv('ACCESS_TOKEN')
API_BASE_URL = "https://api.staircrusher.club"

headers = {
    "Authorization": f"Bearer {ACCESS_TOKEN}",
    "Content-Type": "application/json",
}

def parse_bool(value: str) -> bool:
    return value.strip().upper() == "TRUE"

def parse_floors(value: str):
    value = value.strip()
    if not value:
        return None
    try:
        return [int(v) for v in value.split(",,") if v.strip().isdigit()]
    except ValueError:
        return None

def parse_stair_info(value: str):
    return value.strip() if value.strip() else "NONE"

def parse_stair_height_level(value: str):
    return value.strip() if value.strip() else None

def parse_entrance_door_types(value: str):
    if not value:
        return None
    value = value.strip()
    if not value:
        return None
    return [v.strip() for v in value.split(",,") if v.strip()]

def main():
    # https://docs.google.com/spreadsheets/d/1eXSuPTPLhDMkzVcWYS38KUeE93adnVRulgHfTOEx88Q/edit?gid=0#gid=0
    with open("apply_accessibility_inspect_result/place_accessibility_update.tsv", newline="", encoding="utf-8") as tsvfile:
        reader = csv.DictReader(tsvfile, delimiter="\t")
        for row in reader:
            try:
                place_accessibility_id = row["place_accessibility_id"]

                payload = {
                    "isFirstFloor": parse_bool(row["is_first_floor"]),
                    "stairInfo": parse_stair_info(row["stair_info"]),
                    "hasSlope": parse_bool(row["has_slope"]),
                    "floors": parse_floors(row["floors"]),
                    "isStairOnlyOption": None,
                    "stairHeightLevel": parse_stair_height_level(row["stair_height_level"]),
                    "entranceDoorTypes": parse_entrance_door_types(row["entrance_door_types"]),
                }

                url = f"{API_BASE_URL}/admin/place-accessibilities/{place_accessibility_id}"
                response = requests.put(url, headers=headers, data=json.dumps(payload))

                print(f"[{response.status_code}] {place_accessibility_id} {response.text}")
            except Exception as e:
                print(f"[Error] {place_accessibility_id} {e}")

if __name__ == '__main__':
    main()
