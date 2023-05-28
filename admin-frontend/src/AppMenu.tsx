import {Link, useNavigate} from 'react-router-dom';
import {Button, Navbar, NavbarDivider, NavbarGroup} from "@blueprintjs/core";
import {clearSavedAccessToken} from './context/AuthContext';
import {deployEnvironment, DeployEnvironmentType} from "./config";

function AppMenu() {
  const navigate = useNavigate();

  function logout() {
    clearSavedAccessToken();
    navigate('/login');
  }

  let title = '계단정복지도 어드민';
  switch (deployEnvironment) {
    case DeployEnvironmentType.LOCAL: {
      title += ' (로컬)';
      break;
    }
    case DeployEnvironmentType.DEV: {
      title += ' (DEV)';
      break;
    }
  }

  return (
    <Navbar>
      <NavbarGroup>
        <Link to="/">
          <Button minimal={true} text={title}></Button>
        </Link>
        <NavbarDivider></NavbarDivider>
        <Link to="/clubQuests">
          <Button minimal={true} text="퀘스트 관리"></Button>
        </Link>
        <Link to="/accessibilityAllowedRegions">
          <Button minimal={true} text="정보 등록 허용 지역 관리"></Button>
        </Link>
      </NavbarGroup>
      <NavbarGroup align="right">
        <Button minimal={true} text="로그아웃" onClick={logout}></Button>
      </NavbarGroup>
    </Navbar>
  );
}

export default AppMenu;
