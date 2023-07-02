import { useState } from 'react';
import { Button } from '@blueprintjs/core';
import { Navigate, useNavigate, useSearchParams } from 'react-router-dom';
import { ACCESS_TOKEN_HEADER, AdminApi } from '../../AdminApi';
import { saveAccessToken } from '../../context/AuthContext';

interface LoginPageProps {
  isAuthenticated: () => boolean;
}

function LoginPage(props: LoginPageProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  
  const [searchParams] = useSearchParams();
  const nextUrl = searchParams.get('nextUrl') || '/';
  const navigate = useNavigate();

  if (props.isAuthenticated()) {
    return <Navigate to={nextUrl} />;
  }

  function withLoading(promise: Promise<any>): Promise<any> {
    setIsLoading(true);
    return promise.finally(() => setIsLoading(false));
  }

  async function login() {
    return withLoading(
      AdminApi.loginPost({
        username,
        password,
      }).then((res) => {
        saveAccessToken(res.headers[ACCESS_TOKEN_HEADER]);
        navigate(nextUrl);
      })
    );
  }

  return (
    <div>
      <div>
        <span>아이디 :</span>
        <input value={username} onKeyDown={(e) => {if (e.key === 'Enter') login() }} onChange={({ target: { value }}) => setUsername(value)} />
      </div>
      <div>
        <span>패스워드 :</span>
        <input type="password" value={password} onKeyDown={(e) =>{ if (e.key === 'Enter') login() }} onChange={({ target: { value }}) => setPassword(value)} />
      </div>
      <Button text="로그인" onClick={login} disabled={isLoading}></Button>
    </div>
  );
}

export default LoginPage;
