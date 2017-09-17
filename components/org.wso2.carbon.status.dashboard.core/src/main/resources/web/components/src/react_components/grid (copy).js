import React from 'react';
import {GridList, GridTile} from 'material-ui/GridList';
import IconButton from 'material-ui/IconButton';
import Subheader from 'material-ui/Subheader';
import StarBorder from 'material-ui/svg-icons/toggle/star-border';

const styles = {
  root: {
    display: 'flex',
    flexWrap: 'wrap',
    justifyContent: 'space-around',
  },
  gridList: {
    width: '80%',
    height: 700,
    overflowY: 'auto',
    padding: 10,
  },
};
const styles2 = {
  root: {
    display: 'flex',
    flexWrap: 'wrap',
    justifyContent: 'space-around',
  },
  gridList: {
    width: '100%',
    height: 700,
    overflowY: 'auto',
  },
};

const tilesData = [
  {
    img: 'images/grid-list/test1.jpg',
    title: '10.100.5.41:4040',
    author: 'Last updated 5 mins ago',
    clusterId: 'clusterA',
    names: ['A','B','C','D'],
    value: [15,12,13,14,7],
  },
  {
    img: 'images/grid-list/test2.jpg',
    title: '10.100.5.42:4045',
    author: 'Last updated 5 mins ago',
    clusterId: 'clusterA',
    names: ['A','BB','C','D'],
    value: [15,12,13,14,7],
  },
  {
    img: 'images/grid-list/test3.jpg',
    title: '10.100.5.43:8080',
    author: 'Last updated 5 mins ago',
    clusterId: 'clusterA',
    names: ['A','B','CC','D'],
    value: [15,12,13,14,7],
  },
  {
    img: 'images/grid-list/test4.jpg',
    title: '10.100.5.44:9000',
    author: 'fancycrave1',
    clusterId: 'clusterA',
    names: ['A','B','C','DDe'],
    value: [15,12,13,14,7],
  },
  {
    img: 'images/grid-list/test5.png',
    title: '10.100.5.45:5005',
    author: 'Last updated 5 mins ago',
    clusterId: 'clusterA',
    names: ['A1','B','C','D'],
    value: [15,12,13,14],
  },
  {
    img: 'images/grid-list/test6.png',
    title: '10.100.5.46:9005',
    author: 'Last updated 5 mins ago',
    clusterId: 'clusterA',
    names: ['A','B2','C','D'],
    value: [15,12,13,14,7],
  },
  {
    img: 'images/grid-list/test1.jpg',
    title: '10.100.5.47:8005',
    author: 'Last updated 5 mins ago',
    clusterId: 'clusterA',
    names: ['A','B','C3','D'],
    value: [15,12,13,14,7],
  },
  {
    img: 'images/grid-list/test1.jpg',
    title: '10.100.5.48:8055',
    author: 'Last updated 5 mins ago',
    clusterId: 'clusterA',
    names: ['A','B','C','D4'],
    value: [15,12,13,14,7],
  },
];

var i = 0;
var numRows = 4;
/**
 * A simple example of a scrollable `GridList` containing a [Subheader](/#/components/subheader).
 */
const GridListExampleSimple = () => (
  <div style={styles.root}>
   <GridList
      cols={3}
      cellHeight={180}
      style={styles.gridList}
      >
      <Subheader>December</Subheader>
      {tilesData.map((tile) => (

      <GridTile
      key={tile.img}
      title={tile.title}
      subtitle={<span>{tile.author}<b> &nbsp; &nbsp; &nbsp; &nbsp;  &nbsp; &nbsp; &nbsp; &nbsp;  &nbsp; &nbsp; &nbsp; &nbsp;  &nbsp; &nbsp; &nbsp;  &nbsp; &nbsp; &nbsp; &nbsp;  &nbsp; &nbsp; &nbsp; &nbsp;  {tile.clusterId}  </b></span>}
      actionIcon={
      <IconButton>
         <StarBorder color="white" />
      </IconButton>
      }
      >

      <div style={styles.root}>

         <GridList
            cols={4}
            cellHeight={150}
            style={styles2.gridList}
            >

   {tilesData.map((tile) => (
<GridTile>
</GridTile>
        <div><h2>{tile.value[i]}</h2>
        <p><span>{tile.names[i++]}</span></p></div>

))          
         </GridList>
      
      </div>

      </GridTile>

      ))}

   </GridList>
</div>
);

export default GridListExampleSimple;
