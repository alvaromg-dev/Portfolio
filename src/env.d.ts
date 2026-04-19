/// <reference types="astro/client" />

declare global {
  namespace App {
    interface Locals {
      user?: {
        id: string;
        name: string;
        roles: string[];
      };
    }
  }
}

export {};
