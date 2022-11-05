import { Navigate, Outlet, useLocation } from "react-router-dom";
import AuthContext from './context/AuthContext';

function AuthGuard() {
  const location = useLocation();

  return (
    <AuthContext.Consumer>
      {
        authContext => authContext.isAuthenticated
          ? <Outlet />
          : <Navigate to={`/login?nextUrl=${location.pathname}`} replace />
      }
    </AuthContext.Consumer>
  );
}

export default AuthGuard;
