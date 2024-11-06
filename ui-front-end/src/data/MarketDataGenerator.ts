import { Table } from "@vuu-ui/vuu-data-test";
import { VuuRange } from "@vuu-ui/vuu-protocol-types";
import { generateMarketDepth } from "./data-utils";

const UPDATE_FREQUENCY = 250; // original is 250

interface UpdateGenerator {
  setTable: (table: Table) => void;
  setRange: (range: VuuRange) => void;
}

export class MarketDataGenerator implements UpdateGenerator {
  private table: Table | undefined;
  private range: VuuRange | undefined;
  private updating = false;

  setRange(range: VuuRange) {
    this.range = range;
    if (!this.updating && this.table) {
      this.startUpdating();
    }
  }

  setTable(table: Table) {
    this.table = table;
  }

  private startUpdating() {
    this.updating = true;
    this.update();
  }

  update = () => {
    if (this.range && this.table) {
      const updatedMarketData = generateMarketDepth("VOD.L");
      for (const updatedRow of updatedMarketData) {
        this.table.updateRow(updatedRow);
      }
    }

    if (this.updating) {
      window.setTimeout(this.update, UPDATE_FREQUENCY);
    }
  };
}
