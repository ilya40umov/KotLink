package org.kotlink.namespace

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import javax.validation.Valid

@RestController
@RequestMapping("/namespace")
class NamespaceController(private val namespaceRepo: NamespaceRepo) {

    @GetMapping
    fun findAll(): List<Namespace> = namespaceRepo.findAll()

    @GetMapping("/{id}")
    fun findById(@PathVariable("id") id: Long): ResponseEntity<Namespace> =
        namespaceRepo.findById(id).let {
            return if (it == null) {
                ResponseEntity(HttpStatus.NOT_FOUND)
            } else {
                ResponseEntity(it, HttpStatus.OK)
            }
        }

    @PostMapping
    fun create(@Valid @RequestBody namespace: Namespace, ucBuilder: UriComponentsBuilder): ResponseEntity<Void> {
        if (namespaceRepo.findByKeyword(namespace.keyword) != null) {
            return ResponseEntity(HttpStatus.CONFLICT)
        }
        val assignedId = namespaceRepo.insert(namespace.copy(id = null))
        return ResponseEntity(HttpHeaders().also {
            it.location = ucBuilder.path("/namespace/{id}").buildAndExpand(assignedId).toUri()
        }, HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Void> {
        if (namespaceRepo.findById(id) == null) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        namespaceRepo.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}