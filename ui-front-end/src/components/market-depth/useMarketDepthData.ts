import { SubscribeCallback, TableSchema } from "@vuu-ui/vuu-data-types";
import { useCallback, useMemo, useState } from "react";
import { VuuDataRow } from "@vuu-ui/vuu-protocol-types";
import { ColumnMap, buildColumnMap } from "@vuu-ui/vuu-utils";
import { useDataSource } from "@vuu-ui/vuu-shell";

export type MarketDepthRow = {
  symbolLevel: string;
  offer: number;
  offerQuantity: number;
  bid: number;
  bidQuantity: number;
  level: number;
};

const byLevel = (
  { level: levelA }: MarketDepthRow,
  { level: levelB }: MarketDepthRow
) => (levelA > levelB ? 1 : -1);

class MarketPriceLevelStore {
  #data: Map<string, MarketDepthRow> = new Map();
  #columnMap: ColumnMap;
  constructor(columnMap: ColumnMap) {
    this.#columnMap = columnMap;
    console.log({ columnMap });
  }

  get data() {
    return Array.from(this.#data.values()).sort(byLevel);
  }

  toMarketDepthRow = (vuuRow: VuuDataRow): MarketDepthRow => {
    return {
      symbolLevel: vuuRow[this.#columnMap.symbolLevel] as string,
      level: vuuRow[this.#columnMap.level] as number,
      bidQuantity: vuuRow[this.#columnMap.bidQuantity] as number,
      bid: vuuRow[this.#columnMap.bid] as number,
      offer: vuuRow[this.#columnMap.offer] as number,
      offerQuantity: vuuRow[this.#columnMap.offerQuantity] as number,
    };
  };

  update(vuuData: VuuDataRow[]) {
    const marketDepthRows = vuuData.map(this.toMarketDepthRow);
    marketDepthRows.forEach((row) => {
      this.#data.set(row.symbolLevel, row);
    });
  }
}

export const useMarketDepthData = (schema: TableSchema) => {
  const [, forceUpdate] = useState<unknown>(null);
  const dataStore = useMemo(() => {
    const columnMap = buildColumnMap(schema.columns);
    return new MarketPriceLevelStore(columnMap);
  }, [schema.columns]);

  const { VuuDataSource } = useDataSource();

  const datasourceMessageHandler: SubscribeCallback = useCallback(
    (message) => {
//       console.log({ message });
      if (message.type === "viewport-update") {
        if (message.rows) {
          dataStore.update(message.rows);
          forceUpdate({});
        }
      }
    },
    [dataStore]
  );

  useMemo(() => {
    const ds = new VuuDataSource({
      table: { module: "ALGO", table: "prices" },
    });
    ds.subscribe(
      {
        columns: schema.columns.map((col) => col.name),
        range: { from: 0, to: 10 },
        sort: { sortDefs: [{ column: "level", sortType: "A" }] },
      },
      datasourceMessageHandler
    );

    return ds;
  }, [VuuDataSource, datasourceMessageHandler, schema.columns]);

  return dataStore.data;
};
