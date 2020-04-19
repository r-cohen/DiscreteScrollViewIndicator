# DiscreteScrollViewIndicator
A page indicator for [DiscreteScrollView](https://github.com/yarolegovich/DiscreteScrollView) Android library

## Simple Usage
```java
DiscreteScrollViewIndicator pageIndicator = DiscreteScrollViewIndicator.Builder(discreteScrollView);
discreteScrollView.addItemDecoration(pageIndicator);
```

## Customize
```java
float DP = Resources.getSystem().getDisplayMetrics().density;
DiscreteScrollViewIndicator pageIndicator = DiscreteScrollViewIndicator.Builder(discreteScrollView)
	.setColorActive(getColor(R.color.ap_white))
	.setColorInactive(getColor(R.color.pagerItem))
	.setIndicatorStrokeWidth(4 * DP)
	.setIndicatorItemPadding(10 * DP)
	.align(DiscreteScrollViewIndicator.Alignment.PARENT_TOP)
	.matchParentWidth();
discreteScrollView.addItemDecoration(pageIndicator);
```

## Usage with InfinitePagerAdapter & InfiniteScrollAdapter
The indicator automatically attempts to fetch the number of items through the adapter unless you set it manually as follows.
```java
pageIndicator.setItemsCount(itemsCount);
```

### Inspired by
[bleeding182's recyclerviewItemDecorations](https://github.com/bleeding182/recyclerviewItemDecorations)
