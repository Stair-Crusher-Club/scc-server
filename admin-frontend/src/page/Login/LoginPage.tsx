import { useState } from 'react';
import { Button } from '@blueprintjs/core';
import { Navigate, useNavigate, useSearchParams } from 'react-router-dom';

interface LoginPageProps {
  isAuthenticated: boolean;
  setIsAuthenticated: (value: boolean) => void;
}

function LoginPage(props: LoginPageProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [userId, setUserId] = useState('');
  const [password, setPassword] = useState('');
  
  const [searchParams] = useSearchParams();
  const nextUrl = searchParams.get('nextUrl') || '/';
  const navigate = useNavigate();

  if (props.isAuthenticated) {
    return <Navigate to="/" />;
  }

  function login() {
    if (userId === 'admin' && password === '2022stair!') { // TODO: ㅋㅋㅋㅋ 완전 땜빵
      props.setIsAuthenticated(true);
      navigate(nextUrl);
    } else {
      alert('아이디나 패스워드가 잘못되었습니다.');
    }
  }

  return (
    <div>
      <div>
        <span>아이디 :</span>
        <input value={userId} onChange={({ target: { value }}) => setUserId(value)} />
      </div>
      <div>
        <span>패스워드 :</span>
        <input type="password" value={password} onChange={({ target: { value }}) => setPassword(value)} />
      </div>
      <Button text="로그인" onClick={login} disabled={isLoading}></Button>
    </div>
  );
}

export default LoginPage;
