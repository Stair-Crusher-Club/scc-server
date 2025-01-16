## 애플 클라이언트 JWT 갱신

`secret.txt` 파일을 열면 team id, service id, key id, 그리고 authKey 가 저장되어 있다.
authKey 에 해당하는 내용을 `authkey.p8` 파일을 만들어 붙여넣고 `generate_client_secret.py` 에 적절한 값을 넣어 실행합니다
service id 와 key id 는 테섭용과 실섭용이 구분되어 있으니 스크립트를 두번 돌려야 한다.
