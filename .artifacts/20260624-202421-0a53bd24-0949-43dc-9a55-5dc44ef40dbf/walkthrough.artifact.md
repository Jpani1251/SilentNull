# Walkthrough - Inventory System Implementation

I have implemented the inventory system with a fade transition as requested. The inventory is accessible via a button in the HUD and features a smooth fade-to-black effect when opening and closing.

## Changes

### UI Component

#### [InventoryManager.kt](file:///C:/Users/USUARIO/Desktop/ESCOM/SilentNull/core/src/main/kotlin/com/escom/silentnull/ui/InventoryManager.kt)
- Created a new class to manage the inventory UI.
- Loads `inventory.png` as the background.
- Implements a fade-to-black transition using `ShapeRenderer` and `GL20.GL_BLEND`.
- Includes "INV" (Open) and "VOLVER" (Close) buttons.
- Handles input to block game interaction when the inventory is open.

### Screens Integration

#### [JuegoScreen.kt](file:///C:/Users/USUARIO/Desktop/ESCOM/SilentNull/core/src/main/kotlin/com/escom/silentnull/screens/JuegoScreen.kt)
- Integrated `InventoryManager`.
- The inventory button is rendered in the HUD (top-right).
- Game logic is paused when the inventory is visible or transitioning.

#### [Edificio1Screen.kt](file:///C:/Users/USUARIO/Desktop/ESCOM/SilentNull/core/src/main/kotlin/com/escom/silentnull/screens/Edificio1Screen.kt)
- Integrated `InventoryManager` following the same pattern as `JuegoScreen`.

## Verification Summary

### Automated Tests
- Ran `:core:assemble` and the build finished successfully.
- Fixed a `java.lang.IllegalStateException: SpriteBatch.begin must be called before draw` error that occurred when opening the inventory due to an incorrect `batch.end()` / `batch.begin()` sequence in `InventoryManager.render`.

### Manual Verification
1. **Open Inventory**: Tap the "INV" button in the top-right corner. The screen should fade to black and then show the inventory background.
2. **Close Inventory**: Tap the "VOLVER" button inside the inventory. The screen should fade to black and then return to the game.
3. **Input Blocking**: While the inventory is open or transitioning, you should not be able to move the player or interact with other game elements.
