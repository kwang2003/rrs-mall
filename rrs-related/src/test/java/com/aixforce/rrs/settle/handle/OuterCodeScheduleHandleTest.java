package com.aixforce.rrs.settle.handle;

import com.aixforce.common.model.Response;
import com.aixforce.rrs.jde.mdm.JdeMdmRequest;
import com.aixforce.rrs.jde.mdm.MdmPagingResponse;
import com.aixforce.rrs.jde.mdm.MdmUpdating;
import com.aixforce.rrs.settle.service.SettlementService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 *
 * 测试88码自动更新的场景
 *
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-31 5:33 PM  <br>
 * Author: xiao
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JdeMdmRequest.class)
public class OuterCodeScheduleHandleTest {

    @InjectMocks
    private OuterCodeScheduleHandle outerCodeScheduleHandle;

    @Mock
    private SettlementService settlementService;

    @Mock
    private ShopService shopService;

    private Date now = DateTime.now().toDate();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    private JdeMdmRequest getMockedJdeMdmRequest() {
        JdeMdmRequest mockedPagingResponse = mock(JdeMdmRequest.class);
        when(mockedPagingResponse.startAt(any(Date.class))).thenReturn(mockedPagingResponse);
        when(mockedPagingResponse.endAt(any(Date.class))).thenReturn(mockedPagingResponse);
        return mockedPagingResponse;
    }

    /**
     * 获取一个分页的MDM响应
     *
     * @param pageNo    页码
     * @return  MDM响应
     */
    private MdmPagingResponse getMdmPagingResponse(int pageNo) {
        MdmPagingResponse response = new MdmPagingResponse();
        response.setNext(pageNo == 1 ? Boolean.FALSE : Boolean.TRUE);

        List<MdmUpdating> mdmUpdatings = Lists.newArrayList();

        for (int i = 1; i <= 200; i++ ) {
            MdmUpdating mock = new MdmUpdating();
            mock.setTaxNo(UUID.randomUUID().toString());
            String serial = "000" + i;
            serial = serial.substring(serial.length() - 3, serial.length());
            mock.setOuterCode("8800000" + serial);
            mock.setUpdatedAt(DateTime.now().toDate());
            mdmUpdatings.add(mock);

        }

        response.setResult(mdmUpdatings);
        return response;
    }


    /**
     * 测试正确的场景
     */
    @Test
    public void testSyncOuterCodeSuccess() {
        PowerMockito.mockStatic(JdeMdmRequest.class);
        JdeMdmRequest mdmRequest = getMockedJdeMdmRequest();
        when(JdeMdmRequest.build(anyString())).thenReturn(mdmRequest);

        when(mdmRequest.pageNo(anyInt())).thenReturn(mdmRequest);
        when(mdmRequest.load(anyInt())).thenReturn(getMdmPagingResponse(1));


        Shop shop = new Shop();
        Response<List<Shop>> shopResponse = new Response<List<Shop>>();
        shopResponse.setResult(Lists.newArrayList(shop));

        Response<Boolean> settleResponse = new Response<Boolean>();
        settleResponse.setResult(Boolean.TRUE);

        when(shopService.batchUpdateOuterCodeWithTaxNo(anyString(), anyString())).thenReturn(shopResponse);
        when(settlementService.batchUpdateOuterCodeOfShopRelated(anyString(), any(Shop.class))).thenReturn(settleResponse);

        outerCodeScheduleHandle.syncOuterCode(now);

        verify(shopService, times(200)).batchUpdateOuterCodeWithTaxNo(anyString(), anyString());
        verify(settlementService, times(200)).batchUpdateOuterCodeOfShopRelated(anyString(), any(Shop.class));
    }

    /**
     * 未知异常
     */
    @Test
    public void testSyncOuterCodeWithUnexpectedException() {
        PowerMockito.mockStatic(JdeMdmRequest.class);
        JdeMdmRequest mdmRequest = getMockedJdeMdmRequest();
        when(JdeMdmRequest.build(anyString())).thenReturn(mdmRequest);

        when(mdmRequest.pageNo(anyInt())).thenReturn(mdmRequest);
        when(mdmRequest.load(anyInt())).thenThrow(new NullPointerException("12345"));


        try {
            outerCodeScheduleHandle.syncOuterCode(now);
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("12345");
        }
    }

    /**
     * 预期的异常
     */
    @Test
    public void testSyncOuterCodeWithExceptedException() {
        PowerMockito.mockStatic(JdeMdmRequest.class);
        JdeMdmRequest mdmRequest = getMockedJdeMdmRequest();
        when(JdeMdmRequest.build(anyString())).thenReturn(mdmRequest);

        when(mdmRequest.pageNo(anyInt())).thenReturn(mdmRequest);
        when(mdmRequest.load(anyInt())).thenThrow(new IllegalStateException("12345"));

        outerCodeScheduleHandle.syncOuterCode(now);
    }

}
