import { Navigate } from "react-router-dom";
import AuthContext from './context/AuthContext';

interface AuthRouteProps {
  Component: any;
}

function AuthGuard(props: AuthRouteProps) {
  const { Component, ...rest } = props;
  return (
    <AuthContext.Consumer>
      {
        authContext => authContext.isAuthenticated
          ? <Component {...rest} />
          : <Navigate to="/login" replace />
      }
    </AuthContext.Consumer>
  );
}

export default AuthGuard;
