## 애플 로그인용 Client Secret 갱신

`secret.txt` 파일을 열면 team id, service id, key id, 그리고 authKey 가 저장되어 있습니다.
authKey 에 해당하는 내용을 `authkey.p8` 파일을 만들어 붙여넣고 `generate_client_secret.py` 에 적절한 값을 넣어 실행합니다.
service id, key id 그리고 authKey 는 테섭용과 실섭용이 구분되어 있으니 각각 스크립트에 입력하고 두번 실행해야 합니다.

JWT 의 최대 유효기간이 6개월이라서 6개월마다 갱신해야 합니다.