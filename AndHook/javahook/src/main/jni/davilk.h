//
// Created by panda on 17/11/1.
//

#ifndef ANDHOOK_DAVILK_H
#define ANDHOOK_DAVILK_H

#include <stdint.h>
#include <string.h>

typedef uint8_t             u1;
typedef uint16_t            u2;
typedef uint32_t            u4;
typedef uint64_t            u8;
typedef int8_t              s1;
typedef int16_t             s2;
typedef int32_t             s4;
typedef int64_t             s8;
typedef void (*DalvikBridgeFunc)(const u4* args, void* pResult,
                                 const void* method, void* self);
union JValue {
//#if defined(HAVE_LITTLE_ENDIAN)
    u1      z;
    s1      b;
    u2      c;
    s2      s;
    s4      i;
    s8      j;
    float   f;
    double  d;
    void* l;
};
/* flags for dvmMalloc */
enum {
    ALLOC_DEFAULT = 0x00,
    ALLOC_DONT_TRACK = 0x01,  /* don't add to internal tracking list */
            ALLOC_NON_MOVING = 0x02,
};
struct Object {
    /* ptr to class object */
    void*    clazz;

    /*
     * A word containing either a "thin" lock or a "fat" monitor.  See
     * the comments in Sync.c for a description of its layout.
     */
    u4              lock;
};
#define CLASS_FIELD_SLOTS   4
enum ClassStatus {
    CLASS_ERROR         = -1,

    CLASS_NOTREADY      = 0,
    CLASS_IDX           = 1,    /* loaded, DEX idx in super or ifaces */
            CLASS_LOADED        = 2,    /* DEX idx values resolved */
            CLASS_RESOLVED      = 3,    /* part of linking */
            CLASS_VERIFYING     = 4,    /* in the process of being verified */
            CLASS_VERIFIED      = 5,    /* logically part of linking; done pre-init */
            CLASS_INITIALIZING  = 6,    /* class init in progress */
            CLASS_INITIALIZED   = 7,    /* ready to go */
};
enum PrimitiveType {
    PRIM_NOT        = 0,       /* value is a reference type, not a primitive type */
            PRIM_VOID       = 1,
    PRIM_BOOLEAN    = 2,
    PRIM_BYTE       = 3,
    PRIM_SHORT      = 4,
    PRIM_CHAR       = 5,
    PRIM_INT        = 6,
    PRIM_LONG       = 7,
    PRIM_FLOAT      = 8,
    PRIM_DOUBLE     = 9,
};
struct InitiatingLoaderList {
    /* a list of initiating loader Objects; grown and initialized on demand */
    Object**  initiatingLoaders;
    /* count of loaders in the above list */
    int       initiatingLoaderCount;
};
struct ClassObject : Object {
    /* leave space for instance data; we could access fields directly if we
       freeze the definition of java/lang/Class */
    u4              instanceData[CLASS_FIELD_SLOTS];

    /* UTF-8 descriptor for the class; from constant pool, or on heap
       if generated ("[C") */
    const char*     descriptor;
    char*           descriptorAlloc;

    /* access flags; low 16 bits are defined by VM spec */
    u4              accessFlags;

    /* VM-unique class serial number, nonzero, set very early */
    u4              serialNumber;

    /* DexFile from which we came; needed to resolve constant pool entries */
    /* (will be NULL for VM-generated, e.g. arrays and primitive classes) */
    void*         pDvmDex;

    /* state of class initialization */
    ClassStatus     status;

    /* if class verify fails, we must return same error on subsequent tries */
    ClassObject*    verifyErrorClass;

    /* threadId, used to check for recursive <clinit> invocation */
    u4              initThreadId;

    /*
     * Total object size; used when allocating storage on gc heap.  (For
     * interfaces and abstract classes this will be zero.)
     */
    size_t          objectSize;

    /* arrays only: class object for base element, for instanceof/checkcast
       (for String[][][], this will be String) */
    ClassObject*    elementClass;

    /* arrays only: number of dimensions, e.g. int[][] is 2 */
    int             arrayDim;

    /* primitive type index, or PRIM_NOT (-1); set for generated prim classes */
    PrimitiveType   primitiveType;

    /* superclass, or NULL if this is java.lang.Object */
    ClassObject*    super;

    /* defining class loader, or NULL for the "bootstrap" system loader */
    Object*         classLoader;

    /* initiating class loader list */
    /* NOTE: for classes with low serialNumber, these are unused, and the
       values are kept in a table in gDvm. */
    InitiatingLoaderList initiatingLoaderList;

    /* array of interfaces this class implements directly */
    int             interfaceCount;
    ClassObject**   interfaces;

    /* static, private, and <init> methods */
    int             directMethodCount;
    void*         directMethods;

    /* virtual methods defined in this class; invoked through vtable */
    int             virtualMethodCount;
    void*         virtualMethods;

    /*
     * Virtual method table (vtable), for use by "invoke-virtual".  The
     * vtable from the superclass is copied in, and virtual methods from
     * our class either replace those from the super or are appended.
     */
    int             vtableCount;
    void**        vtable;

    /*
     * Interface table (iftable), one entry per interface supported by
     * this class.  That means one entry for each interface we support
     * directly, indirectly via superclass, or indirectly via
     * superinterface.  This will be null if neither we nor our superclass
     * implement any interfaces.
     *
     * Why we need this: given "class Foo implements Face", declare
     * "Face faceObj = new Foo()".  Invoke faceObj.blah(), where "blah" is
     * part of the Face interface.  We can't easily use a single vtable.
     *
     * For every interface a concrete class implements, we create a list of
     * virtualMethod indices for the methods in the interface.
     */
    int             iftableCount;
    void* iftable;

    /*
     * The interface vtable indices for iftable get stored here.  By placing
     * them all in a single pool for each class that implements interfaces,
     * we decrease the number of allocations.
     */
    int             ifviPoolCount;
    int*            ifviPool;

    /* instance fields
     *
     * These describe the layout of the contents of a DataObject-compatible
     * Object.  Note that only the fields directly defined by this class
     * are listed in ifields;  fields defined by a superclass are listed
     * in the superclass's ClassObject.ifields.
     *
     * All instance fields that refer to objects are guaranteed to be
     * at the beginning of the field list.  ifieldRefCount specifies
     * the number of reference fields.
     */
    int             ifieldCount;
    int             ifieldRefCount; // number of fields that are object refs
    void*      ifields;

    /* bitmap of offsets of ifields */
    u4 refOffsets;

    /* source file name, if known */
    const char*     sourceFile;

    /* static fields */
    int             sfieldCount;
};
struct Field {
    ClassObject*    clazz;          /* class in which the field is declared */
    const char*     name;
    const char*     signature;      /* e.g. "I", "[C", "Landroid/os/Debug;" */
    u4              accessFlags;
};
struct ArrayObject : Object {
    /* number of elements; immutable after init */
    u4              length;

    /*
     * Array contents; actual size is (length * sizeof(type)).  This is
     * declared as u8 so that the compiler inserts any necessary padding
     * (e.g. for EABI); the actual allocation may be smaller than 8 bytes.
     */
    u8              contents[1];
};
struct DexProto {
    const void* dexFile;     /* file the idx refers to */
    u4 protoIdx;                /* index into proto_ids table of dexFile */
};
struct Method {
    /* the class we are a part of */
    ClassObject*    clazz;

    /* access flags; low 16 bits are defined by spec (could be u2?) */
    u4              accessFlags;

    /*
     * For concrete virtual methods, this is the offset of the method
     * in "vtable".
     *
     * For abstract methods in an interface class, this is the offset
     * of the method in "iftable[n]->methodIndexArray".
     */
    u2             methodIndex;

    /*
     * Method bounds; not needed for an abstract method.
     *
     * For a native method, we compute the size of the argument list, and
     * set "insSize" and "registerSize" equal to it.
     */
    u2              registersSize;  /* ins + locals */
    u2              outsSize;
    u2              insSize;

    /* method name, e.g. "<init>" or "eatLunch" */
    const char*     name;

    /*
     * Method prototype descriptor string (return and argument types).
     *
     * TODO: This currently must specify the DexFile as well as the proto_ids
     * index, because generated Proxy classes don't have a DexFile.  We can
     * remove the DexFile* and reduce the size of this struct if we generate
     * a DEX for proxies.
     */
    DexProto        prototype;

    /* short-form method descriptor string */
    const char*     shorty;

    /*
     * The remaining items are not used for abstract or native methods.
     * (JNI is currently hijacking "insns" as a function pointer, set
     * after the first call.  For internal-native this stays null.)
     */

    /* the actual code */
    const u2*       insns;          /* instructions, in memory-mapped .dex */

    /* JNI: cached argument and return-type hints */
    int             jniArgInfo;

    /*
     * JNI: native method ptr; could be actual function or a JNI bridge.  We
     * don't currently discriminate between DalvikBridgeFunc and
     * DalvikNativeFunc; the former takes an argument superset (i.e. two
     * extra args) which will be ignored.  If necessary we can use
     * insns==NULL to detect JNI bridge vs. internal native.
     */
     void * func;

    /*
     * JNI: true if this static non-synchronized native method (that has no
     * reference arguments) needs a JNIEnv* and jclass/jobject. Libcore
     * uses this.
     */
    bool fastJni;

    /*
     * JNI: true if this method has no reference arguments. This lets the JNI
     * bridge avoid scanning the shorty for direct pointers that need to be
     * converted to local references.
     *
     * TODO: replace this with a list of indexes of the reference arguments.
     */
    bool noRef;

    /*
     * JNI: true if we should log entry and exit. This is the only way
     * developers can log the local references that are passed into their code.
     * Used for debugging JNI problems in third-party code.
     */
    bool shouldTrace;

    /*
     * Register map data, if available.  This will point into the DEX file
     * if the data was computed during pre-verification, or into the
     * linear alloc area if not.
     */
    const void* registerMap;

    /* set if method was called during method profiling */
    bool            inProfile;
};
bool dvmIsConstructorMethod(const Method* method) {
    return (method->accessFlags & kAccConstructor) != 0;
}
Object* (* dvmDecodeIndirectRef)(void* self, jobject jobj);
void* (*dvmThreadSelf)();
void (*dvmCallMethod)(void* self, const Method* method, Object* obj,void* pResult, ...);
//void dvmReleaseTrackedAlloc(Object* obj, Thread* self)
void (*dvmReleaseTrackedAlloc)(Object* obj, void* self);
//ClassObject* dvmGetBoxedReturnType(const Method* meth)
//_Z21dvmGetBoxedReturnTypePK6Method
ClassObject* (*dvmGetBoxedReturnType)(const Method* meth);
//bool dvmUnboxPrimitive(Object* value, ClassObject* returnType,JValue* pResult)
//_Z17dvmUnboxPrimitiveP6ObjectP11ClassObjectP6JValue
bool (*dvmUnboxPrimitive)(Object* value, ClassObject* returnType,void* pResult);
//size_t dexProtoGetParameterCount(const DexProto* pProto)
//_Z25dexProtoGetParameterCountPK8DexProto
size_t (*dexProtoGetParameterCount)(const DexProto* pProto);
//ArrayObject* dvmAllocArrayByClass(ClassObject* arrayClass,size_t length, int allocFlags)
ArrayObject* (*dvmAllocArrayByClass)(ClassObject* arrayClass,size_t length, int allocFlags);
//ClassObject* dvmFindSystemClass(const char* descriptor)
//_Z18dvmFindSystemClassPKc
ClassObject* (*dvmFindSystemClass)(const char* descriptor);
//ClassObject* dvmFindPrimitiveClass(char type)
ClassObject* (*dvmFindPrimitiveClass)(char type);
void* (*dvmBoxPrimitive)(JValue value, ClassObject* returnType);
void* (*dvmSetNativeFunc)(void*,void*,void*);
void* (*dvmInvokeMethod)(void* obj, void* method,
                         void* argList, void* params, void* returnType,
                         bool noAccessCheck);
void* (*dvmGetMethodFromReflectObj)(void*);

struct XposedHookInfo {
    struct {
        Method originalMethod;
        // copy a few bytes more than defined for Method in AOSP
        // to accomodate for (rare) extensions by the target ROM
        int dummyForRomExtensions[4];
    } originalMethodStruct;

    Object* reflectedMethod;
    Object* callback;
};
void hookedMethodCallback(const u4* args, JValue* pResult, const Method* method, void* self);
inline bool isMethodHooked(const Method* method) {
    return (method->func == &hookedMethodCallback);
}
bool dvmIsStaticMethod(const Method* method) {
    return (method->accessFlags & kAccStatic) != 0;
}
bool dvmIsPrimitiveClass(const ClassObject* clazz) {
    return clazz->primitiveType != PRIM_NOT;
}
s8 dvmGetArgLong(const u4* args, int elem)
{
    s8 val;
    memcpy(&val, &args[elem], sizeof(val));
    return val;
}
ArrayObject* dvmBoxMethodArgs(const Method* method, const u4* args){
    const char* desc = &method->shorty[1]; // [0] is the return type.

    /* count args */
    size_t argCount = dexProtoGetParameterCount(&method->prototype);

    static ClassObject* java_lang_object_array = dvmFindSystemClass("[Ljava/lang/Object;");

    /* allocate storage */
    ArrayObject* argArray = dvmAllocArrayByClass(java_lang_object_array, argCount, ALLOC_DEFAULT);
    if (argArray == NULL)
        return NULL;

    Object** argObjects = (Object**) (void*) argArray->contents;

    /*
     * Fill in the array.
     */
    size_t srcIndex = 0;
    size_t dstIndex = 0;
    while (*desc != '\0') {
        char descChar = *(desc++);
        JValue value;

        switch (descChar) {
            case 'Z':
            case 'C':
            case 'F':
            case 'B':
            case 'S':
            case 'I':
                value.i = args[srcIndex++];
                argObjects[dstIndex] = (Object*) dvmBoxPrimitive(value, dvmFindPrimitiveClass(descChar));
                /* argObjects is tracked, don't need to hold this too */
                dvmReleaseTrackedAlloc(argObjects[dstIndex], NULL);
                dstIndex++;
                break;
            case 'D':
            case 'J':
                value.j = dvmGetArgLong(args, srcIndex);
                srcIndex += 2;
                argObjects[dstIndex] = (Object*) dvmBoxPrimitive(value, dvmFindPrimitiveClass(descChar));
                dvmReleaseTrackedAlloc(argObjects[dstIndex], NULL);
                dstIndex++;
                break;
            case '[':
            case 'L':
                argObjects[dstIndex++] = (Object*) args[srcIndex++];
                break;
        }
    }

    return argArray;
}
#endif //ANDHOOK_DAVILK_H
