import { useState } from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import HomePage from './page/Home/HomePage';
import LoginPage from './page/Login/LoginPage';
import ClubQuestsPage from './page/ClubQuests/ClubQuestsPage';
import CreateClubQuestPage from './page/CreateClubQuest/CreateClubQuestPage';
import ClubQuestPage from './page/ClubQuest/ClubQuestPage';
import AccessibilityAllowedRegionsPage from "./page/AccessibilityAllowedRegions/AccessibilityAllowedRegionsPage";
import CreateAccessibilityAllowedRegionPage
  from "./page/CreateAccessibilityAllowedRegion/CreateAccessibilityAllowedRegionPage";
import AccessibilityAllowedRegionPage from "./page/AccessibilityAllowedRegion/AccessibilityAllowedRegionPage";
import AuthContext, { getSavedAccessToken } from './context/AuthContext';
import PageLayout from './PageLayout';
import AuthGuard from './AuthGuard';
import "./App.scss";

function App() {
  const isAuthenticated = () => {
    return getSavedAccessToken() != null;
  }

  return (
    <div>
      <AuthContext.Provider value={{ isAuthenticated }}>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage isAuthenticated={isAuthenticated} />} />
            <Route path="/" element={<PageLayout />}>
              <Route path="/clubQuests/:id" element={<ClubQuestPage />} />
              <Route path="/" element={<AuthGuard />}>
                <Route path="/" element={<HomePage />} />
                <Route path="clubQuests" element={<ClubQuestsPage />} />
                <Route path="clubQuest/create" element={<CreateClubQuestPage />} />
                <Route path="accessibilityAllowedRegions" element={<AccessibilityAllowedRegionsPage />} />
                <Route path="accessibilityAllowedRegion/create" element={<CreateAccessibilityAllowedRegionPage />} />
                <Route path="accessibilityAllowedRegions/:id" element={<AccessibilityAllowedRegionPage />} />
              </Route>
            </Route>
          </Routes>
        </BrowserRouter>
      </AuthContext.Provider>
    </div>
  );
}

export default App;
