package com.bsoft.timeseries.delegate;

import com.bsoft.timeseries.api.AddressesApiDelegate;
import com.bsoft.timeseries.model.Address;
import com.bsoft.timeseries.model.AddressPage;
import com.bsoft.timeseries.model.AddressRequest;
import com.bsoft.timeseries.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static com.bsoft.timeseries.delegate.DelegateSupport.resolveTransactionAt;
import static com.bsoft.timeseries.delegate.DelegateSupport.resolveValidAt;

//@Service("mainAddressDelegate")
//@Primary
/*
@RequiredArgsConstructor
public class AddressesApiDelegateImpl implements AddressesApiDelegate {

    private final AddressService addressService;

    @Override
    public ResponseEntity<AddressPage> listAddresses(OffsetDateTime validAt,
                                                     OffsetDateTime transactionAt,
                                                     Integer page,
                                                     Integer size) {
        return ResponseEntity.ok(addressService.listAtPoint(
                resolveValidAt(validAt),
                resolveTransactionAt(transactionAt),
                page != null ? page : 0,
                size != null ? size : 20));
    }

    @Override
    public ResponseEntity<Address> createAddress(AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressService.create(request));
    }

    @Override
    public ResponseEntity<Address> getAddress(UUID addressId,
                                              OffsetDateTime validAt,
                                              OffsetDateTime transactionAt) {
        return ResponseEntity.ok(addressService.findAtPoint(
                addressId,
                resolveValidAt(validAt),
                resolveTransactionAt(transactionAt)));
    }

    @Override
    public ResponseEntity<Address> updateAddress(UUID addressId,
                                                 AddressRequest request) {
        return ResponseEntity.ok(addressService.update(addressId, request));
    }

    @Override
    public ResponseEntity<Void> terminateAddress(UUID addressId,
                                                 OffsetDateTime validTo) {
        addressService.terminate(addressId, validTo);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<Address>> getAddressHistory(UUID addressId) {
        return ResponseEntity.ok(addressService.history(addressId));
    }
}

 */