// Hamkkebu Keycloak Theme - Style Fixes
document.addEventListener('DOMContentLoaded', function() {
  // Fix password toggle button
  const toggleButtons = document.querySelectorAll('.pf-c-button.pf-m-control, .pf-c-input-group button');
  toggleButtons.forEach(function(btn) {
    btn.style.cssText = 'all: unset !important; cursor: pointer !important; color: rgba(255,255,255,0.4) !important; padding: 8px !important; position: absolute !important; right: 8px !important; top: 50% !important; transform: translateY(-50%) !important;';
  });

  // Fix input group wrapper
  const inputGroups = document.querySelectorAll('.pf-c-input-group__item');
  inputGroups.forEach(function(item) {
    item.style.cssText = 'all: unset !important; position: absolute !important; right: 0 !important; top: 0 !important; bottom: 0 !important; display: flex !important; align-items: center !important;';
  });
});
