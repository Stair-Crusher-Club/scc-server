import { Link, useNavigate } from 'react-router-dom';
import { Button, Navbar, NavbarDivider, NavbarGroup } from "@blueprintjs/core";
import { clearSavedAccessToken } from './context/AuthContext';

function AppMenu() {
  const navigate = useNavigate();

  function logout() {
    clearSavedAccessToken();
    navigate('/login');
  }
  return (
    <Navbar>
      <NavbarGroup>
        <Link to="/">
          <Button minimal={true} text="계단정복지도 어드민"></Button>
        </Link>
        <NavbarDivider></NavbarDivider>
        <Link to="/clubQuests">
          <Button minimal={true} text="퀘스트 관리"></Button>
        </Link>
      </NavbarGroup>
      <NavbarGroup align="right">
        <Button minimal={true} text="로그아웃" onClick={logout}></Button>
      </NavbarGroup>
    </Navbar>
  );
}

export default AppMenu;
