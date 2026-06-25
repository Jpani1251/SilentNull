# Implementation Plan - Inventory System

Add an inventory system with a fade transition, triggered by an "Inventario" button.

## User Review Required

> [!IMPORTANT]
> The inventory will be implemented as an overlay on top of the current screen. It will include a fade-to-black transition when opening and closing, similar to the entry video transition mentioned by the user.

- **Button Placement**: The "Inventario" button will be placed in the HUD (top-right corner by default).
- **Closing the Inventory**: Since not specified, I'll implement a "Regresar" button inside the inventory to close it.
- **Screen Integration**: I will start by adding the inventory to `JuegoScreen` and `Edificio1Screen`. If the user wants it in more screens, they can specify.

## Proposed Changes

### UI Component

#### [NEW] [InventoryManager.kt](file:///C:/Users/USUARIO/Desktop/ESCOM/SilentNull/core/src/main/kotlin/com/escom/silentnull/ui/InventoryManager.kt)

- Handle loading `inventory.png`.
- Manage fade state and animation.
- Handle "Inventario" and "Regresar" buttons.
- Render the inventory overlay.

### Screens

#### [JuegoScreen.kt](file:///C:/Users/USUARIO/Desktop/ESCOM/SilentNull/core/src/main/kotlin/com/escom/silentnull/screens/JuegoScreen.kt)

- Add `InventoryManager` instance.
- Update and render `InventoryManager`.
- Pass input to `InventoryManager`.

#### [Edificio1Screen.kt](file:///C:/Users/USUARIO/Desktop/ESCOM/SilentNull/core/src/main/kotlin/com/escom/silentnull/screens/Edificio1Screen.kt)

- Add `InventoryManager` instance.
- Update and render `InventoryManager`.
- Pass input to `InventoryManager`.

## Verification Plan

### Manual Verification
- Deploy the app.
- Go to `JuegoScreen`.
- Tap the "Inventario" button.
- Verify the fade transition to black and then the inventory background appears.
- Tap the "Regresar" button in the inventory.
- Verify the fade transition back to the game.
- Repeat for `Edificio1Screen`.
