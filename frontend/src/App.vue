<script setup>
import { RouterView } from 'vue-router'
import RentalAlertsGlobal from './components/RentalAlertsGlobal.vue'
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-950 transition-colors duration-300">
    <RentalAlertsGlobal />
    <RouterView />
  </div>
</template>

<style>
@import "tailwindcss";
</style>
