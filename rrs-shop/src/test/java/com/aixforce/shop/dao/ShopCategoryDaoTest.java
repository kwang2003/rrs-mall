package com.aixforce.shop.dao;

import com.aixforce.shop.model.ShopCategory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-10-30
 */
public class ShopCategoryDaoTest extends BaseDaoTest {

    @Autowired
    private ShopCategoryDao shopCategoryDao;

    private ShopCategory cat1;

    private ShopCategory cat2;

    private ShopCategory cat11;

    private ShopCategory cat12;

    private ShopCategory cat21;

    @Before
    public void setUp() throws Exception {
        cat1 = makeShopCategory("cat1", 0L);
        cat2 = makeShopCategory("cat2", 0L);

        shopCategoryDao.create(cat1);
        shopCategoryDao.create(cat2);

        cat11 = makeShopCategory("cat11", cat1.getId());
        cat12 = makeShopCategory("cat12", cat1.getId());

        cat21 = makeShopCategory("cat21", cat2.getId());

        shopCategoryDao.create(cat11);
        shopCategoryDao.create(cat12);

        shopCategoryDao.create(cat21);

    }


    @Test
    public void testFindByParentId() throws Exception {
        List<ShopCategory> cats = shopCategoryDao.findByParentId(cat1.getId());
        assertThat(cats, contains(cat11, cat12));
    }

    @Test
    public void testFindById() throws Exception {
        assertThat(shopCategoryDao.findById(cat1.getId()), is(cat1));
    }

    @Test
    public void testUpdateName() throws Exception {
        shopCategoryDao.updateName(cat1.getId(), "new cat1");
        assertThat(shopCategoryDao.findById(cat1.getId()).getName(), is("new cat1"));
    }

    @Test
    public void testDelete() throws Exception {
        shopCategoryDao.delete(cat1.getId());
        assertThat(shopCategoryDao.findById(cat1.getId()), nullValue());
    }


    private ShopCategory makeShopCategory(String name, Long parentId) {
        ShopCategory shopCategory = new ShopCategory();
        shopCategory.setName(name);
        shopCategory.setParentId(parentId);
        return shopCategory;
    }
}
