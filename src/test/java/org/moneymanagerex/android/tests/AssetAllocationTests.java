/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.moneymanagerex.android.tests;

import android.content.Context;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.datalayer.AssetClassRepository;
import com.money.manager.ex.datalayer.AssetClassStockRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.domainmodel.AssetClassStock;
import com.money.manager.ex.domainmodel.Stock;
import com.money.manager.ex.servicelayer.AssetAllocationService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for asset allocation service.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class AssetAllocationTests {

    private AssetAllocationService testObject;

    @Before
    public void setup() {
        this.testObject = new AssetAllocationService(UnitTestHelper.getContext());
    }

    @After
    public void tearDown() {
        this.testObject = null;
    }

    @Test
    public void testInstantiation() {
        assertThat(testObject).isNotNull();
    }

    @Test
    public void testDataLayer() {
        // Given

        UnitTestHelper.initializeContentProvider();
        Context context = UnitTestHelper.getContext();
//        StockRepository stockRepository = new StockRepository(context);
        AssetClassRepository classRepo = new AssetClassRepository(context);
        AssetClassStockRepository classStockRepo = new AssetClassStockRepository(context);
        Double expectedAllocation = 14.28;
        int expectedAssetClassId = 1;

        // save
        createRecords(context);

        // When

        // test loading

        AssetClass actualClass = classRepo.load(expectedAssetClassId);
        actualClass.setStockLinks(classStockRepo.loadForClass(expectedAssetClassId));

        // Then

        assertThat(actualClass).isNotNull();
        assertThat(actualClass.getAllocation()).isEqualTo(expectedAllocation);

        assertThat(actualClass.getStockLinks()).isNotNull();
        assertThat(actualClass.getStockLinks().size()).isGreaterThan(0);
    }

    @Test
    public void testLoadingOfAllocation() {
        // Given

        UnitTestHelper.initializeContentProvider();
        Context context = UnitTestHelper.getContext();
        createRecords(context);

        // When

        List<AssetClass> actual = testObject.loadAssetAllocation();

        // Then

        assertThat(actual).isNotNull();
        assertThat(actual.size()).isGreaterThan(0);
        // There are two elements at the level 0.
        assertThat(actual.size()).isEqualTo(2);
        // The second element is a group with a child element
        AssetClass second = actual.get(1);
        assertThat(second.getChildren().size()).isEqualTo(1);
        // test total calculation on the group element
        AssetClass child = second.getChildren().get(0);
        assertThat(second.getAllocation()).isEqualTo(child.getAllocation());

        //test calculation of current allocation by adding the value of all related stocks
        assertThat(child.getStockLinks().size()).isGreaterThan(0);
        assertThat(child.getStocks().size()).isGreaterThan(0);

        Money expectedSum = testObject.getStockValue(child.getStocks());
//        assertThat(child.getAllocation()).isEqualTo(expectedSum);
//        assertThat(second.getAllocation()).isEqualTo(expectedSum);
        assertThat(child.getCurrentValue()).isEqualTo(expectedSum);
        assertThat(second.getCurrentValue()).isEqualTo(expectedSum);
    }

    /**
     * test calculating the stock value.
     */
    @Test
    public void calculateStockValue() {
        // Given
        Stock stock1 = Stock.getInstance();
        stock1.setNumberOfShares(50.0);
        stock1.setCurrentPrice(MoneyFactory.fromString("12.00"));
        // 600

        Stock stock2 = Stock.getInstance();
        stock2.setNumberOfShares(23.45);
        stock2.setCurrentPrice(MoneyFactory.fromString("7.68"));
        // 180.096

        List<Stock> stocks = new ArrayList<>();
        stocks.add(stock1);
        stocks.add(stock2);

        Money expected = MoneyFactory.fromString("780.096");

        // When

        Money actual = this.testObject.getStockValue(stocks);

        // Then

        assertThat(actual).isEqualTo(expected);
    }

    // Private

    public void createRecords(Context context) {
        boolean created;
        AssetClassRepository classRepo = new AssetClassRepository(context);
        AssetClassStockRepository classStockRepo = new AssetClassStockRepository(context);

        // Create stocks

        // stock 1
        StockRepository stockRepo = new StockRepository(context);
        Stock stock1 = Stock.getInstance();
        stock1.setName("stock1");
        stock1.setCurrentPrice(MoneyFactory.fromString("10"));
        stock1.setNumberOfShares(3.0);
        created = stockRepo.insert(stock1);
        assertThat(created).isTrue();
        // stock 2
        Stock stock2 = Stock.getInstance();
        stock2.setName("stock2");
        stock2.setCurrentPrice(MoneyFactory.fromString("13.24"));
        stock2.setNumberOfShares(2.0);
        created = stockRepo.insert(stock2);
        assertThat(created).isTrue();

        // Asset Allocation

        // One root element with allocation.
        AssetClass class1 = AssetClass.create();
        class1.setName("class1");
        class1.setAllocation(14.28);
        created = classRepo.insert(class1);
        assertThat(created).isTrue();

        AssetClassStock link1 = AssetClassStock.create();
        link1.setAssetClassId(class1.getId());
        link1.setStockId(1);
        created = classStockRepo.insert(link1);
        assertThat(created).isTrue();

//        class1.addStockLink(link1);
//        class1.addStock(stock1);

        // One group with child allocation.

        AssetClass class2 = AssetClass.create();
        class2.setName("class2");
        class2.setAllocation(25.16);
        created = classRepo.insert(class2);
        assertThat(created).isTrue();

        // child
        AssetClass class2child = AssetClass.create();
        class2child.setName("class2child");
        class2child.setParentId(class2.getId());
        class2child.setAllocation(25.16);
        created = classRepo.insert(class2child);
        assertThat(created).isTrue();

        // add stock links
        AssetClassStock classStock1 = AssetClassStock.create();
        classStock1.setAssetClassId(class2child.getId());
        classStock1.setStockId(2);
        created = classStockRepo.insert(classStock1);
        assertThat(created).isTrue();

//        class2child.addStockLink(classStock1);
        // add stock(s)
//        class2child.addStock(stock2);
    }

}