#include <stdlib.h>

#include "memory.h"

/*
 * Covers the four scenarios of allocation:
 *  1. From zero to non-zero (malloc)
 *  2. From non-zero to zero (free)
 *  3. Grow existing allocation  (realloc)
 *  4. Grow shrinking allocation (realloc)
 * 
 * 'realloc' covers cases 2-4.
*/
void* reallocate(void *pointer, size_t oldSize, size_t newSize) {
    if (newSize == 0) {
        free(pointer);
        return NULL;
    }

    void* result = realloc(pointer, newSize);
    if (result == NULL) {
        exit(1); // Not enough space in memory.
    }

    return result;
}
