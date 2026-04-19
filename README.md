# Portfolio - Álvaro Martín Granados

Personal portfolio website built with Astro.

🌐 **[alvaromg.com](https://alvaromg.com)**

## Environment Setup

The app runs as an Astro SSR app on Node using environment-specific files:

- `.env.dev`
- `.env.pre`
- `.env.pro`

Available commands:

```bash
npm run dev:dev
npm run dev:pre
npm run dev:pro

npm run build:dev
npm run build:pre
npm run build:pro

npm run start:dev
npm run start:pre
npm run start:pro

npm run serve:dev
npm run serve:pre
npm run serve:pro
```

Notes:

- `dev:*` runs Astro in the selected mode and loads `.env.<mode>`.
- `build:*` builds with the selected mode.
- `start:*` starts the built Node server and loads `.env.<mode>` at runtime.
- `serve:*` builds and starts in one command.

## 🛠️ Technologies

- **[Astro](https://astro.build/)** - Static Site Generator

## 📊 CV Data Management

All portfolio content (experience, education, projects, skills) is managed through a single **`cv.json`** file, making updates simple and centralized.

## 📧 Contact

- **Email**: alvaromg.004@gmail.com
- **LinkedIn**: [Álvaro Martín Granados](https://www.linkedin.com/in/%C3%A1lvaro-mart%C3%ADn-granados-9b6b48256/)
- **GitHub**: [@alvaromg-dev](https://github.com/alvaromg-dev)

## 📄 License

Copyright (c) 2025 Álvaro Martín Granados
All rights reserved.

Redistribution, modification, or public use of the code, content, or personal data contained in this repository is prohibited without prior express authorization from the owner.

