package com.yerandis.sge.entity.system;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();

    public static void set(UUID tenantId)  { TENANT_ID.set(tenantId); }
    public static UUID  get()              { return TENANT_ID.get(); }
    public static void  clear()            { TENANT_ID.remove(); }
}
