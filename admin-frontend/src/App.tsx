import { useState } from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import HomePage from './page/Home/HomePage';
import LoginPage from './page/Login/LoginPage';
import ClubQuestsPage from './page/ClubQuests/ClubQuestsPage';
import CreateClubQuestPage from './page/CreateClubQuest/CreateClubQuestPage';
import AuthContext from './context/AuthContext';
import AuthenticatedPageLayout from './AuthenticatedPageLayout';

import "./App.scss";

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  return (
    <div>
      <AuthContext.Provider value={{ isAuthenticated, setIsAuthenticated }}>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage isAuthenticated={isAuthenticated} setIsAuthenticated={setIsAuthenticated} />} />
            <Route path="/" element={<AuthenticatedPageLayout />}>
              <Route path="/" element={<HomePage />} />
              <Route path="clubQuests" element={<ClubQuestsPage />} />
              <Route path="clubQuest/create" element={<CreateClubQuestPage />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </AuthContext.Provider>
    </div>
  );
}

export default App;
