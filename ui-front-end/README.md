# Coding Black Females - UI Exercise - build a Depth of Market View

# Setting up yout dev environment

## install pre-requisites - all you need is node.js

well, technically you need `npm` too, but that ships as part of node.js

For any references to version numbers below, just use the latest available, e.g today the latest v20 is v20.17.0, the minor numbers don't matter much, just take the latest

For our UI exercise, Node.js version required: 18+

Stable Node versions are the even-numbered ones. Although latest available version is 22, the current default is version 20. If you do not already have Node installed, I would recommend v20 . If you already have v18, that will be fine. If you have an older version than 18, it might be ok if its not too much older, but some of the modern build tools might require a more recent version. Try it - if you can follow the instructions below with no errors, its ok.

check your version of node from the command line ...

```bash
node -v
```

If you need to, download Node from the official site
https://nodejs.org/en

## Get the code for the UI exercise

Pull down latest code, everything you need for the UI exercise is in the folder `ui-front-end`,
This is a `vite` project, meaning it was created with the `npm create vite` command and has vite build tools installed. see https://vitejs.dev/guide/ for more info.

## install dependencies

Use `ui-front-end` as your working folder (i.e. open that folder in VSCode).
From the root (i.e from within `ui-front-end`), install dependencies ...

```
 npm install
```

or same thing, more succinctly

```
npm i
```

This will download all the dependencies (declared in package.json) and save them in a local `node_modules` folder within `ui-front-end`

## Run the App

There is a "scripts" section in package.json, these are npm scripts that you can run from the command line (in VSCode you can also run them from the "NPM SCRIPTS" section at the bottom of the `Explorer` panel) . These are standard scripts, created when vite first initialised the project

YOU ONLY NEED TO DO THE FIRST OF THE STEPS BELOW, THE REST SHOWN JUST FOR COMPLETENESS

- Run the vite dev server, which will host the app on http://localhost:5173, with full Hot Module Loading (change the source code, app will be updated automatically])

```bash
npm run dev
```

You should see output with a link to tell you that app is running on localhost:5173. You will be able to open http://localhost:5173 in your browser (google Chrome recommended)

For future reference ...

- Build the app, does a 'production' build - output will go in the `dist` folder

```
npm run build
```

- launch the app using production build, will host app on http://localhost:4173

```
npm run preview
```

Note: mostly you will be using `npm run dev` to run the dev server, or as most developers would describe it, you will be running in dev mode.

Finally, if you want to run the linter (eslint) across the codebase - you will see how clean your types are. Note not having your types perfectly defined will NOT stop the app from running. It is possible for the app to run perfectly correctly even when the types are wrong. Remember, types are there to help you in the IDE, they do not affect the running code.

```bash
npm run lint
```

## What is the aim of the exercise

Run the app, using `npm run dev`

You will see a table of data in the upper half of the screen. Below is a Placeholder with a button that will show you a screenshot of a `Market Depth` component. The aim is to actually
build a component like the one in the screenshot. The data is provided for you, see instructions below.

## Where do I begin the exercise

Within the `src` folder, all your work will live inside the `market-depth` folder.
You will remove the <Placeholder /> component and replace it with a <MarketDepthPanel /> component (which you will create).

There area two commented out import statements at the top of MarketDepthFeature.tsx

```typescript
import { useMarketDepthData } from "./useMarketDepthData";
import { schemas } from "../../data/algo-schemas";
```

and a corresponding usage of this react `hook` and the schemas

```typescript
const data = useMarketDepthData(schemas.prices);
```

You will begin by uncommenting these. These will provide you with the data that will be rendered in the new MarketDepthFeature component. This data will refresh very frequently.

## Steps to start the exercise

1. MarketDepthFeature.tsx , uncomment the block of static data on lines 6 - 17
2. still in MarketDepthFeature.tsx, delete <PlaceHolder /> on line 24
3. create a new File MarketDepthPanel.tsx
4. In this new file export a new component, called MarketDepthPanel, the overall structure will look something like this

```typescript
interface MarketDepthPanelProps {
  data: MarketDepthRow[];
}

export const MarketDepthPanel = (props: MarketDepthPanelProps) => {
  console.log({ props });
  return <table></table>;
};
```

5. Back in MarketDepthFeature.tsx, import the new component and use it

```typescript
import {MarketDepthPanel} from './MarketDepthPanel';

return <MarketDepthPanel data={testData}>

```

6. Fill out the content of the table in MarketDepthPanel, building the appropriate rows,columns
7. Add a CSS file MarketDepthPanel.css, import it in MarketDepthPanel.tsx

```typescript
import "./MarketDepthPanel.css";
```

8. give the table a className that tou can reference in the css file

```typescript
<table className="MarketDepthPanel"></table>
```

9. then, in the css file ...

```css
.MarketDepthPanel {
  /*... your css here */
}
```

10. When you have the structure of the table completed, you can add a component for the Price cell, create a new file PriceCell.tsx
11. The structure will be something like this

```

export interface PriceCellProps {
    price: number;
}

export const PriceCell = (props: PriceCellProps) => {
    const {price} = props;
    return (
        <td>{price}</td>
    )
}

```

12. then you can use this component in your table

```
import {PriceCell} from './PriceCell';

return (
    <table>
        <tr>
            <td></td>
            <PriceCell price={price}/>
             <td></td>
             ... rest of td ...
       </tr>
       ... rest of tr ...
    </table>
)
```

### Example of useRef hook

This is the example I showed in StackBlitz, this was the ShowDirection component. The usage of useRef
here should serve as an example of how you could implement the PriceCell component

```typescript
import { useRef } from "react";

interface ShowDirectionProps {
  value: number;
}

export const ShowDirection = (props: ShowDirectionProps) => {
  const lastValueRef = useRef(props.value);

  const diff = props.value - lastValueRef.current;

  lastValueRef.current = props.value;

  console.log(`diff = ${diff}`);

  return <div>{`Show Direction diff = ${diff}`}</div>;
};
```
