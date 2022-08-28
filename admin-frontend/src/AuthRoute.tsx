import { Route, Navigate } from "react-router-dom";
import AuthContext from './context/AuthContext';

interface AuthRouteProps {
  element: any;
  path: string;
}

function AuthRoute(props: AuthRouteProps) {
  const { path, element, ...rest } = props;
  return (
    <AuthContext.Consumer>
      {
        authContext => authContext.isAuthenticated
          ? <Route path={props.path} element={element} {...rest} />
          : <Navigate to="/login" replace />
      }
    </AuthContext.Consumer>
  );
}

export default AuthRoute;
