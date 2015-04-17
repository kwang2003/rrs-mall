package com.aixforce.item.service;

import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.item.dao.mysql.BrandDao;
import com.aixforce.item.model.Brand;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangzefeng on 14-1-15
 */
@Service
public class BrandServiceImpl implements BrandService{

    private final static Logger log = LoggerFactory.getLogger(BrandServiceImpl.class);

    @Autowired
    private BrandDao brandDao;

    private LoadingCache<Long, Optional<Brand>> brandCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build(
            new CacheLoader<Long,Optional<Brand>>() {

        @Override
        public Optional<Brand> load(Long key) throws Exception {
            return Optional.fromNullable(brandDao.findById(key));
        }
    });

    @Override
    public Response<List<Brand>> findAll() {
        Response<List<Brand>> result = new Response<List<Brand>>();
        try {
            List<Brand> brands = brandDao.findAll();
            result.setResult(brands);
            return result;
        }catch (Exception e) {
            log.error("failed to find all brand, cause:", e);
            result.setError("brand.query.fail");
            return result;
        }
    }


    @Override
    public Response<Brand> findById(Long id) {
        Response<Brand> result = new Response<Brand>();
        try {
            Optional<Brand> ob = brandCache.getUnchecked(id);
            if(ob.isPresent()){
                result.setResult(ob.get());
                return result;
            }else {
                log.error("failed to find brand(id={})", id);
                result.setError("brand.query.fail");
                return result;
            }
        }catch (Exception e) {
            log.error("failed to find brand by id={}, cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("brand.query.fail");
            return result;
        }
    }

    @Override
    public Response<Long> create(Brand brand) {
        Response<Long> result = new Response<Long>();

        if(brand == null) {
            log.error("brand can not be null when create brand");
            result.setError("illegal.param");
            return result;
        }
        if(brand.getName() == null) {
            log.error("brand name can not be null when create brand");
            result.setError("illegal.param");
            return result;
        }

        try {
            Brand exist = brandDao.findByName(brand.getName());
            if(exist != null) {
                log.error("brand name{} duplicate", brand.getName());
                result.setError("brand.name.duplicate");
                return result;
            }
            brandDao.create(brand);
            result.setResult(brand.getId());
            return result;
        }catch (Exception e) {
            log.error("fail to create brand{}, cause:{}", brand, Throwables.getStackTraceAsString(e));
            result.setError("brand.create.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> update(Brand brand) {
        Response<Boolean> result = new Response<Boolean>();
        if(brand.getId() == null) {
            log.error("brand id can not be null when update");
            result.setError("illegal.param");
            return result;
        }
        try {
            Brand exist = brandDao.findByName(brand.getName());
            if(exist != null && !Objects.equal(exist.getId(), brand.getId())) {
                log.error("brand name{} duplicate", brand.getName());
                result.setError("brand.name.duplicate");
                return result;
            }
            brandDao.update(brand);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update brand{}, cause:{}", brand, Throwables.getStackTraceAsString(e));
            result.setError("brand.update.fail");
            return result;
        }
    }

    @Override
    public Response<Paging<Brand>> paging(String name, Integer pageNo, Integer size) {
        PageInfo page = new PageInfo(pageNo, size);
        Response<Paging<Brand>> result = new Response<Paging<Brand>>();

        Paging<Brand> brandPaging;

        try {

            if(Strings.isNullOrEmpty(name)){
                brandPaging = brandDao.paging(page.getOffset(), page.getLimit());
            }else{
                brandPaging = brandDao.pagingByName(name.trim(), page.getOffset(), page.getLimit());
            }
            result.setResult(brandPaging);
            return result;
        } catch (Exception e) {
            log.error("`findByName` invoke fail. e:{}", e);
            result.setError("brand.query.fail");
            return result;
        }
    }
}
