export interface PortfolioBasics {
  name: string;
  label: string;
  image: string;
  email: string;
  status: string;
  profiles: PortfolioProfile[];
}

export interface PortfolioProfile {
  network: string;
  url: string;
}

export interface PortfolioWork {
  name: string;
  position: string;
  url: string | null;
  startDate: string;
  endDate: string | null;
  summary: string;
  highlights: string[];
}

export interface PortfolioProject {
  name: string;
  description: string;
  url: string | null;
  highlights: string[];
}

export interface PortfolioSkill {
  name: string;
}

export interface PortfolioEducation {
  institution: string;
  area: string;
  url: string | null;
  startDate: string;
  endDate: string | null;
  courses: string[];
}

export interface PortfolioData {
  basics: PortfolioBasics | null;
  aboutMe: string;
  work: PortfolioWork[];
  projects: PortfolioProject[];
  skills: PortfolioSkill[];
  education: PortfolioEducation[];
}

export interface LanguageOption {
  code: string;
  name: string;
}

export interface LanguageRow {
  id: string;
  code: string;
  name: string;
  enabled: boolean;
  isDefault: boolean;
  hasData: boolean;
}

export interface ManagedUserRow {
  id: string;
  name: string;
  roleCodes: string[];
  isAdmin: boolean;
}

export interface TelemetryVisitRow {
  id: string;
  visitorId: string;
  ipAddress: string;
  country: string;
  region: string;
  city: string;
  browser: string;
  deviceType: string;
  operatingSystem: string;
  path: string;
  visitedAt: string;
}

export interface TelemetryLoginRow {
  id: string;
  username: string;
  ipAddress: string;
  country: string;
  region: string;
  city: string;
  loggedAt: string;
}

export interface TelemetryStats {
  day: number;
  week: number;
  month: number;
  year: number;
}

export interface HeaderNavOption {
  label: string;
  path: string;
  active: boolean;
}

export interface HeaderNavigationState {
  navOptions: HeaderNavOption[];
  showEditButton: boolean;
  showLoginButton: boolean;
  showLogoutButton: boolean;
  languages: LanguageOption[];
  currentLanguage: string;
}
