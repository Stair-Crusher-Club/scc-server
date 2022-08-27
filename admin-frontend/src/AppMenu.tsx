import { Link } from 'react-router-dom';
import { Button, Navbar, NavbarDivider, NavbarGroup } from "@blueprintjs/core";

const AppMenu = () => (
  <Navbar>
    <NavbarGroup>
      <Link to="/">
        <Button minimal={true} text="계단정복지도 어드민"></Button>
      </Link>
      <NavbarDivider></NavbarDivider>
    </NavbarGroup>
  </Navbar>
);

export default AppMenu;