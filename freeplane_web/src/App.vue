<template>
	<div id="app">
	  <MindMapCanvas />
	</div>
  </template>
  
  <script setup lang="ts">
  import MindMapCanvas from './components/MindMapCanvas.vue'
  </script>
  
  <style>
  * { 
	margin: 0; 
	padding: 0; 
	box-sizing: border-box; 
  }
  
  #app { 
	height: 100vh; 
	width: 100vw; 
	overflow: hidden; 
	font-family: system-ui, sans-serif; 
  }
  </style>