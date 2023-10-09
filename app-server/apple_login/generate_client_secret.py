# https://jkim68888.tistory.com/7
# 클라이언트 팀으로부터 team_id, service_id, key_id, authkey.p8을 전달받아 사용하면 된다.
from time import time
import jwt

team_id = "dummy"
service_id = "dummy"
key_id = "dummy"
expiration_duration_seconds = 6 * 30 * 86400  # maximum 6 months


def generate_token():
    with open("authkey.p8", "r") as f:
        private_key = f.read()
    timestamp_now = int(time())
    timestamp_exp = timestamp_now + expiration_duration_seconds
    data = {
        "iss": team_id,
        "iat": timestamp_now,
        "exp": timestamp_exp,
        "aud": "https://appleid.apple.com",
        "sub": service_id
    }
    token = jwt.encode(payload=data, key=private_key, algorithm="ES256", headers={"kid": key_id})
    print(token)


generate_token()
