import { Navigate, Outlet } from "react-router-dom";
import AppMenu from "./AppMenu";
import AuthContext from './context/AuthContext';

function AuthenticatedPageLayout() {
  return (
    <AuthContext.Consumer>
      {
        authContext => authContext.isAuthenticated
          ? (
            <div>
              <AppMenu />
              <div className="app-body-container">
                <div className="app-body">
                  <Outlet />
                </div>
              </div>
            </div>
          )
          : <Navigate to="/login" replace />
      }
    </AuthContext.Consumer>
  );
}

export default AuthenticatedPageLayout;
