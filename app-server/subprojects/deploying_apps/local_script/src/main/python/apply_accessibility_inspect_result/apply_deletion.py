import requests
from dotenv import load_dotenv
import os
import csv

load_dotenv()

# Constants
base_url = "https://api.staircrusher.club"
access_token = os.getenv('ACCESS_TOKEN')
# https://docs.google.com/spreadsheets/d/1eXSuPTPLhDMkzVcWYS38KUeE93adnVRulgHfTOEx88Q/edit?gid=966009041#gid=966009041
csv_file_path = "apply_accessibility_inspect_result/deletion_target_place_accessibility_ids.csv"

# Load IDs from CSV
accessibility_ids = []
with open(csv_file_path, newline='', encoding='utf-8') as csvfile:
    reader = csv.reader(csvfile)
    for row in reader:
        if row:  # skip empty lines
            accessibility_ids.append(row[0].strip())

headers = {
    "Authorization": f"Bearer {access_token}",
    "Content-Type": "application/json",
}

def main():
    for accessibility_id in accessibility_ids:
        url = f"{base_url}/admin/place-accessibilities/{accessibility_id}"
        response = requests.delete(url, headers=headers, json={})

        print(f"ID: {accessibility_id} | Status: {response.status_code} | Response: {response.text}")

if __name__ == '__main__':
    main()
