import { LeageCreationPageComponent } from "./components/pages/leage-creation-page/leage-creation-page.component";
import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { betroundPage } from "./components/pages/betroundPage/betroundPage";
import { homePage } from "./components/pages/homePage/homePage";
import { GameTableBetsComponent } from "./components/game-table-bets/game-table-bets.component";
import { StatisticsPageComponent } from "./components/pages/statistics/statistics-page/statistics-page.component";
import { MinigameComponent } from "./components/minigame/minigame.component";

const routes: Routes = [
  { path: "", component: homePage },
  { path: "bets-and-pieces", component: betroundPage },
  { path: "ligen-management", component: LeageCreationPageComponent },
  { path: "test", component: GameTableBetsComponent },
  { path: "mini-game", component: MinigameComponent },
  { path: "statistiken", component: StatisticsPageComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
