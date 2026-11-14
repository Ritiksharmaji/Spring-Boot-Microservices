/**
 * Cross-cutting building blocks used by every module: error types and RFC 7807 handling,
 * security configuration and helpers. Declared OPEN so all modules may use it.
 */
@ApplicationModule(displayName = "Shared kernel", type = ApplicationModule.Type.OPEN)
package com.ecommerce.shop.shared;

import org.springframework.modulith.ApplicationModule;
