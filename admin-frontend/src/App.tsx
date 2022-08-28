import { BrowserRouter, Route, Routes } from 'react-router-dom';
import AppMenu from "./AppMenu";
import AuthGuard from './AuthGuard';
import HomePage from './page/Home/HomePage';
import LoginPage from './page/Login/LoginPage';
import AuthContext from './context/AuthContext';

import "./App.scss";
import { useState } from 'react';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  return (
    <div>
      <AuthContext.Provider value={{ isAuthenticated, setIsAuthenticated }}>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage isAuthenticated={isAuthenticated} setIsAuthenticated={setIsAuthenticated} />} />
            <Route path="/" element={
              <div>
                <AppMenu />
                <div className="app-body">
                  <Routes>
                    <Route path= "/" element={<AuthGuard Component={HomePage} />} />
                  </Routes>
                </div>
              </div>
            } />
          </Routes>
        </BrowserRouter>
      </AuthContext.Provider>
    </div>
  );
}

export default App;
