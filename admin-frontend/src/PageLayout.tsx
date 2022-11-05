import { Outlet } from "react-router-dom";
import AppMenu from "./AppMenu";
import AuthContext from "./context/AuthContext";

function PageLayout() {
  return (
    <AuthContext.Consumer>
      {
        (authContext) => {
          return (
            <div>
              {authContext.isAuthenticated ? <AppMenu /> : null}
              <div className="app-body-container">
                <div className="app-body">
                  <Outlet />
                </div>
              </div>
            </div>
          )
        }
      }
    </AuthContext.Consumer>
  );
}

export default PageLayout;
