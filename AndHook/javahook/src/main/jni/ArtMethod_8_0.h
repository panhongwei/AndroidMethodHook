//
// Created by panda on 17/10/17.
//

#ifndef ANDHOOK_ARTMETHOD_8_0_H
#define ANDHOOK_ARTMETHOD_8_0_H
#include <stddef.h>
typedef unsigned char		uint8_t;
typedef unsigned short int	uint16_t;
typedef unsigned int		uint32_t;
class ArtMethod {
public:
// Field order required by test "ValidateFieldOrderOfJavaCppUnionClasses".
// The class we are a part of.
    size_t *declaring_class_;

// Access flags; low 16 bits are defined by spec.
// Getting and setting this flag needs to be atomic when concurrency is
// possible, e.g. after this method's class is linked. Such as when setting
// verifier flags and single-implementation flag.
    uint32_t access_flags_;

/* Dex file fields. The defining dex file is available via declaring_class_->dex_cache_ */

// Offset to the CodeItem.
    uint32_t dex_code_item_offset_;

// Index into method_ids of the dex file associated with this method.
    uint32_t dex_method_index_;

/* End of dex file fields. */

// Entry within a dispatch table for this method. For static/direct methods the index is into
// the declaringClass.directMethods, for virtual methods the vtable and for interface methods the
// ifTable.
    uint16_t method_index_;

// The hotness we measure for this method. Managed by the interpreter. Not atomic, as we allow
// missing increments: if the method is hot, we will see it eventually.
    uint16_t hotness_count_;

// Fake padding field gets inserted here.

// Must be the last fields in the method.
    struct PtrSizedFields {
        // Short cuts to declaring_class_->dex_cache_ member for fast compiled code access.
        ArtMethod **dex_cache_resolved_methods_;

        // Pointer to JNI function registered to this method, or a function to resolve the JNI function,
        // or the profiling data for non-native methods, or an ImtConflictTable, or the
        // single-implementation of an abstract/interface method.
        void *data_;

        // Method dispatch from quick compiled code invokes this pointer which may cause bridging into
        // the interpreter.
        void *entry_point_from_quick_compiled_code_;
    } ptr_sized_fields_;
};
static constexpr uint32_t kAccPublic =       0x0001;  // class, field, method, ic
static constexpr uint32_t kAccPrivate =      0x0002;  // field, method, ic
static constexpr uint32_t kAccProtected =    0x0004;  // field, method, ic
static constexpr uint32_t kAccStatic =       0x0008;  // field, method, ic
static constexpr uint32_t kAccConstructor =           0x00010000;  // method (dex only) <(cl)init>
static bool IsDirect(uint32_t access_flags) {
    constexpr uint32_t direct = kAccStatic | kAccPrivate | kAccConstructor;
    return (access_flags & direct) != 0;
}
#endif //ANDHOOK_ARTMETHOD_8_0_H
