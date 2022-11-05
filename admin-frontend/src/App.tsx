import { useState } from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import HomePage from './page/Home/HomePage';
import LoginPage from './page/Login/LoginPage';
import ClubQuestsPage from './page/ClubQuests/ClubQuestsPage';
import CreateClubQuestPage from './page/CreateClubQuest/CreateClubQuestPage';
import ClubQuestPage from './page/ClubQuest/ClubQuestPage';
import AuthContext from './context/AuthContext';
import PageLayout from './PageLayout';
import AuthGuard from './AuthGuard';

import "./App.scss";

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  return (
    <div>
      <AuthContext.Provider value={{ isAuthenticated, setIsAuthenticated }}>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage isAuthenticated={isAuthenticated} setIsAuthenticated={setIsAuthenticated} />} />
            <Route path="/" element={<PageLayout />}>
              <Route path="/clubQuests/:id" element={<ClubQuestPage />} />
              <Route path="/" element={<AuthGuard />}>
                <Route path="/" element={<HomePage />} />
                <Route path="clubQuests" element={<ClubQuestsPage />} />
                <Route path="clubQuest/create" element={<CreateClubQuestPage />} />
              </Route>
            </Route>
          </Routes>
        </BrowserRouter>
      </AuthContext.Provider>
    </div>
  );
}

export default App;
