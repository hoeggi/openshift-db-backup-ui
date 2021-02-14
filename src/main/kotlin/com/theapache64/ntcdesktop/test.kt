package com.theapache64.ntcdesktop

val SECRET = """
{
    "apiVersion": "v1",
    "items": [
        {
            "apiVersion": "v1",
            "data": {
                "authorized_keys": "c3NoLWVkMjU1MTkgQUFBQUMzTnphQzFsWkRJMU5URTVBQUFBSUEwdThpMERQVkprUGJlbGhLOGoyL1RNV2tqamZKdGZ4UnYwL21kNk5CUUYK",
                "aws-s3-ca.crt": "",
                "aws-s3-key": "",
                "aws-s3-key-secret": "",
                "config": "SG9zdCAqCglTdHJpY3RIb3N0S2V5Q2hlY2tpbmcgbm8KCUlkZW50aXR5RmlsZSAvdG1wL2lkX2VkMjU1MTkKCVBvcnQgMjAyMgoJVXNlciBwZ2JhY2tyZXN0Cg==",
                "id_ed25519": "LS0tLS1CRUdJTiBPUEVOU1NIIFBSSVZBVEUgS0VZLS0tLS0KYjNCbGJuTnphQzFyWlhrdGRqRUFBQUFBQkc1dmJtVUFBQUFFYm05dVpRQUFBQUFBQUFBQkFBQUFNd0FBQUF0egpjMmd0WldReU5UVXhPUUFBQUNBTkx2SXRBejFTWkQyM3BZU3ZJOXYwekZwSTQzeWJYOFViOVA1bmVqUVVCUUFBCkFJaGJsaGMrVzVZWFBnQUFBQXR6YzJndFpXUXlOVFV4T1FBQUFDQU5Mdkl0QXoxU1pEMjNwWVN2STl2MHpGcEkKNDN5Ylg4VWI5UDVuZWpRVUJRQUFBRUJUSU04UlpTL21wamY1N2YzS1N1b0VobG81R1BaKy9xeGZDLzZoMVNtUwpqdzB1OGkwRFBWSmtQYmVsaEs4ajIvVE1Xa2pqZkp0ZnhSdjAvbWQ2TkJRRkFBQUFBQUVDQXdRRgotLS0tLUVORCBPUEVOU1NIIFBSSVZBVEUgS0VZLS0tLS0K",
                "ssh_host_ed25519_key": "LS0tLS1CRUdJTiBPUEVOU1NIIFBSSVZBVEUgS0VZLS0tLS0KYjNCbGJuTnphQzFyWlhrdGRqRUFBQUFBQkc1dmJtVUFBQUFFYm05dVpRQUFBQUFBQUFBQkFBQUFNd0FBQUF0egpjMmd0WldReU5UVXhPUUFBQUNBTkx2SXRBejFTWkQyM3BZU3ZJOXYwekZwSTQzeWJYOFViOVA1bmVqUVVCUUFBCkFJaGJsaGMrVzVZWFBnQUFBQXR6YzJndFpXUXlOVFV4T1FBQUFDQU5Mdkl0QXoxU1pEMjNwWVN2STl2MHpGcEkKNDN5Ylg4VWI5UDVuZWpRVUJRQUFBRUJUSU04UlpTL21wamY1N2YzS1N1b0VobG81R1BaKy9xeGZDLzZoMVNtUwpqdzB1OGkwRFBWSmtQYmVsaEs4ajIvVE1Xa2pqZkp0ZnhSdjAvbWQ2TkJRRkFBQUFBQUVDQXdRRgotLS0tLUVORCBPUEVOU1NIIFBSSVZBVEUgS0VZLS0tLS0K",
                "sshd_config": "IwkkT3BlbkJTRDogc3NoZF9jb25maWcsdiAxLjEwMCAyMDE2LzA4LzE1IDEyOjMyOjA0IG5hZGR5IEV4cCAkCgojIFRoaXMgaXMgdGhlIHNzaGQgc2VydmVyIHN5c3RlbS13aWRlIGNvbmZpZ3VyYXRpb24gZmlsZS4gIFNlZQojIHNzaGRfY29uZmlnKDUpIGZvciBtb3JlIGluZm9ybWF0aW9uLgoKIyBUaGlzIHNzaGQgd2FzIGNvbXBpbGVkIHdpdGggUEFUSD0vdXNyL2xvY2FsL2JpbjovdXNyL2JpbgoKIyBUaGUgc3RyYXRlZ3kgdXNlZCBmb3Igb3B0aW9ucyBpbiB0aGUgZGVmYXVsdCBzc2hkX2NvbmZpZyBzaGlwcGVkIHdpdGgKIyBPcGVuU1NIIGlzIHRvIHNwZWNpZnkgb3B0aW9ucyB3aXRoIHRoZWlyIGRlZmF1bHQgdmFsdWUgd2hlcmUKIyBwb3NzaWJsZSwgYnV0IGxlYXZlIHRoZW0gY29tbWVudGVkLiAgVW5jb21tZW50ZWQgb3B0aW9ucyBvdmVycmlkZSB0aGUKIyBkZWZhdWx0IHZhbHVlLgoKIyBJZiB5b3Ugd2FudCB0byBjaGFuZ2UgdGhlIHBvcnQgb24gYSBTRUxpbnV4IHN5c3RlbSwgeW91IGhhdmUgdG8gdGVsbAojIFNFTGludXggYWJvdXQgdGhpcyBjaGFuZ2UuCiMgc2VtYW5hZ2UgcG9ydCAtYSAtdCBzc2hfcG9ydF90IC1wIHRjcCAjUE9SVE5VTUJFUgojClBvcnQgMjAyMgojQWRkcmVzc0ZhbWlseSBhbnkKI0xpc3RlbkFkZHJlc3MgMC4wLjAuMAojTGlzdGVuQWRkcmVzcyA6OgoKSG9zdEtleSAvc3NoZC9zc2hfaG9zdF9lZDI1NTE5X2tleQoKIyBDaXBoZXJzIGFuZCBrZXlpbmcKI1Jla2V5TGltaXQgZGVmYXVsdCBub25lCgojIExvZ2dpbmcKI1N5c2xvZ0ZhY2lsaXR5IEFVVEgKU3lzbG9nRmFjaWxpdHkgQVVUSFBSSVYKI0xvZ0xldmVsIElORk8KCiMgQXV0aGVudGljYXRpb246CgojTG9naW5HcmFjZVRpbWUgMm0KUGVybWl0Um9vdExvZ2luIG5vClN0cmljdE1vZGVzIG5vCiNNYXhBdXRoVHJpZXMgNgojTWF4U2Vzc2lvbnMgMTAKClB1YmtleUF1dGhlbnRpY2F0aW9uIHllcwoKIyBUaGUgZGVmYXVsdCBpcyB0byBjaGVjayBib3RoIC5zc2gvYXV0aG9yaXplZF9rZXlzIGFuZCAuc3NoL2F1dGhvcml6ZWRfa2V5czIKIyBidXQgdGhpcyBpcyBvdmVycmlkZGVuIHNvIGluc3RhbGxhdGlvbnMgd2lsbCBvbmx5IGNoZWNrIC5zc2gvYXV0aG9yaXplZF9rZXlzCiNBdXRob3JpemVkS2V5c0ZpbGUJL3BnY29uZi9hdXRob3JpemVkX2tleXMKQXV0aG9yaXplZEtleXNGaWxlCS9zc2hkL2F1dGhvcml6ZWRfa2V5cwoKI0F1dGhvcml6ZWRQcmluY2lwYWxzRmlsZSBub25lCgojQXV0aG9yaXplZEtleXNDb21tYW5kIG5vbmUKI0F1dGhvcml6ZWRLZXlzQ29tbWFuZFVzZXIgbm9ib2R5CgojIEZvciB0aGlzIHRvIHdvcmsgeW91IHdpbGwgYWxzbyBuZWVkIGhvc3Qga2V5cyBpbiAvZXRjL3NzaC9zc2hfa25vd25faG9zdHMKI0hvc3RiYXNlZEF1dGhlbnRpY2F0aW9uIG5vCiMgQ2hhbmdlIHRvIHllcyBpZiB5b3UgZG9uJ3QgdHJ1c3Qgfi8uc3NoL2tub3duX2hvc3RzIGZvcgojIEhvc3RiYXNlZEF1dGhlbnRpY2F0aW9uCiNJZ25vcmVVc2VyS25vd25Ib3N0cyBubwojIERvbid0IHJlYWQgdGhlIHVzZXIncyB+Ly5yaG9zdHMgYW5kIH4vLnNob3N0cyBmaWxlcwojSWdub3JlUmhvc3RzIHllcwoKIyBUbyBkaXNhYmxlIHR1bm5lbGVkIGNsZWFyIHRleHQgcGFzc3dvcmRzLCBjaGFuZ2UgdG8gbm8gaGVyZSEKI1Bhc3N3b3JkQXV0aGVudGljYXRpb24geWVzCiNQZXJtaXRFbXB0eVBhc3N3b3JkcyBubwpQYXNzd29yZEF1dGhlbnRpY2F0aW9uIG5vCgojIENoYW5nZSB0byBubyB0byBkaXNhYmxlIHMva2V5IHBhc3N3b3JkcwpDaGFsbGVuZ2VSZXNwb25zZUF1dGhlbnRpY2F0aW9uIHllcwojQ2hhbGxlbmdlUmVzcG9uc2VBdXRoZW50aWNhdGlvbiBubwoKIyBLZXJiZXJvcyBvcHRpb25zCiNLZXJiZXJvc0F1dGhlbnRpY2F0aW9uIG5vCiNLZXJiZXJvc09yTG9jYWxQYXNzd2QgeWVzCiNLZXJiZXJvc1RpY2tldENsZWFudXAgeWVzCiNLZXJiZXJvc0dldEFGU1Rva2VuIG5vCiNLZXJiZXJvc1VzZUt1c2Vyb2sgeWVzCgojIEdTU0FQSSBvcHRpb25zCiNHU1NBUElBdXRoZW50aWNhdGlvbiB5ZXMKI0dTU0FQSUNsZWFudXBDcmVkZW50aWFscyBubwojR1NTQVBJU3RyaWN0QWNjZXB0b3JDaGVjayB5ZXMKI0dTU0FQSUtleUV4Y2hhbmdlIG5vCiNHU1NBUElFbmFibGVrNXVzZXJzIG5vCgojIFNldCB0aGlzIHRvICd5ZXMnIHRvIGVuYWJsZSBQQU0gYXV0aGVudGljYXRpb24sIGFjY291bnQgcHJvY2Vzc2luZywKIyBhbmQgc2Vzc2lvbiBwcm9jZXNzaW5nLiBJZiB0aGlzIGlzIGVuYWJsZWQsIFBBTSBhdXRoZW50aWNhdGlvbiB3aWxsCiMgYmUgYWxsb3dlZCB0aHJvdWdoIHRoZSBDaGFsbGVuZ2VSZXNwb25zZUF1dGhlbnRpY2F0aW9uIGFuZAojIFBhc3N3b3JkQXV0aGVudGljYXRpb24uICBEZXBlbmRpbmcgb24geW91ciBQQU0gY29uZmlndXJhdGlvbiwKIyBQQU0gYXV0aGVudGljYXRpb24gdmlhIENoYWxsZW5nZVJlc3BvbnNlQXV0aGVudGljYXRpb24gbWF5IGJ5cGFzcwojIHRoZSBzZXR0aW5nIG9mICJQZXJtaXRSb290TG9naW4gd2l0aG91dC1wYXNzd29yZCIuCiMgSWYgeW91IGp1c3Qgd2FudCB0aGUgUEFNIGFjY291bnQgYW5kIHNlc3Npb24gY2hlY2tzIHRvIHJ1biB3aXRob3V0CiMgUEFNIGF1dGhlbnRpY2F0aW9uLCB0aGVuIGVuYWJsZSB0aGlzIGJ1dCBzZXQgUGFzc3dvcmRBdXRoZW50aWNhdGlvbgojIGFuZCBDaGFsbGVuZ2VSZXNwb25zZUF1dGhlbnRpY2F0aW9uIHRvICdubycuCiMgV0FSTklORzogJ1VzZVBBTSBubycgaXMgbm90IHN1cHBvcnRlZCBpbiBSZWQgSGF0IEVudGVycHJpc2UgTGludXggYW5kIG1heSBjYXVzZSBzZXZlcmFsCiMgcHJvYmxlbXMuClVzZVBBTSB5ZXMKCiNBbGxvd0FnZW50Rm9yd2FyZGluZyB5ZXMKI0FsbG93VGNwRm9yd2FyZGluZyB5ZXMKI0dhdGV3YXlQb3J0cyBubwpYMTFGb3J3YXJkaW5nIHllcwojWDExRGlzcGxheU9mZnNldCAxMAojWDExVXNlTG9jYWxob3N0IHllcwojUGVybWl0VFRZIHllcwojUHJpbnRNb3RkIHllcwojUHJpbnRMYXN0TG9nIHllcwojVENQS2VlcEFsaXZlIHllcwojVXNlTG9naW4gbm8KVXNlUHJpdmlsZWdlU2VwYXJhdGlvbiBubwojUGVybWl0VXNlckVudmlyb25tZW50IG5vCiNDb21wcmVzc2lvbiBkZWxheWVkCiNDbGllbnRBbGl2ZUludGVydmFsIDAKI0NsaWVudEFsaXZlQ291bnRNYXggMwojU2hvd1BhdGNoTGV2ZWwgbm8KI1VzZUROUyB5ZXMKI1BpZEZpbGUgL3Zhci9ydW4vc3NoZC5waWQKI01heFN0YXJ0dXBzIDEwOjMwOjEwMAojUGVybWl0VHVubmVsIG5vCiNDaHJvb3REaXJlY3Rvcnkgbm9uZQojVmVyc2lvbkFkZGVuZHVtIG5vbmUKCiMgbm8gZGVmYXVsdCBiYW5uZXIgcGF0aAojQmFubmVyIG5vbmUKCiMgQWNjZXB0IGxvY2FsZS1yZWxhdGVkIGVudmlyb25tZW50IHZhcmlhYmxlcwpBY2NlcHRFbnYgTEFORyBMQ19DVFlQRSBMQ19OVU1FUklDIExDX1RJTUUgTENfQ09MTEFURSBMQ19NT05FVEFSWSBMQ19NRVNTQUdFUwpBY2NlcHRFbnYgTENfUEFQRVIgTENfTkFNRSBMQ19BRERSRVNTIExDX1RFTEVQSE9ORSBMQ19NRUFTVVJFTUVOVApBY2NlcHRFbnYgTENfSURFTlRJRklDQVRJT04gTENfQUxMIExBTkdVQUdFCkFjY2VwdEVudiBYTU9ESUZJRVJTCgojIG92ZXJyaWRlIGRlZmF1bHQgb2Ygbm8gc3Vic3lzdGVtcwpTdWJzeXN0ZW0Jc2Z0cAkvdXNyL2xpYmV4ZWMvb3BlbnNzaC9zZnRwLXNlcnZlcgoKIyBFeGFtcGxlIG9mIG92ZXJyaWRpbmcgc2V0dGluZ3Mgb24gYSBwZXItdXNlciBiYXNpcwojTWF0Y2ggVXNlciBhbm9uY3ZzCiMJWDExRm9yd2FyZGluZyBubwojCUFsbG93VGNwRm9yd2FyZGluZyBubwojCVBlcm1pdFRUWSBubwojCUZvcmNlQ29tbWFuZCBjdnMgc2VydmVyCg=="
            },
            "kind": "Secret",
            "metadata": {
                "annotations": {
                    "pg-port": "5432",
                    "repo-path": "/backrestrepo/postgres12-backrest-shared-repo",
                    "s3-bucket": "",
                    "s3-endpoint": "",
                    "s3-region": "",
                    "s3-uri-style": "",
                    "s3-verify-tls": "true",
                    "sshd-port": "2022",
                    "supplemental-groups": ""
                },
                "creationTimestamp": "2020-11-25T09:27:31Z",
                "labels": {
                    "pg-cluster": "postgres12",
                    "pgo-backrest-repo": "true",
                    "vendor": "crunchydata"
                },
                "managedFields": [
                    {
                        "apiVersion": "v1",
                        "fieldsType": "FieldsV1",
                        "fieldsV1": {
                            "f:data": {
                                ".": {},
                                "f:aws-s3-ca.crt": {},
                                "f:aws-s3-key": {},
                                "f:aws-s3-key-secret": {}
                            },
                            "f:metadata": {
                                "f:labels": {
                                    ".": {},
                                    "f:pg-cluster": {},
                                    "f:pgo-backrest-repo": {},
                                    "f:vendor": {}
                                }
                            },
                            "f:type": {}
                        },
                        "manager": "apiserver",
                        "operation": "Update",
                        "time": "2020-11-25T09:27:31Z"
                    },
                    {
                        "apiVersion": "v1",
                        "fieldsType": "FieldsV1",
                        "fieldsV1": {
                            "f:data": {
                                "f:authorized_keys": {},
                                "f:config": {},
                                "f:id_ed25519": {},
                                "f:ssh_host_ed25519_key": {},
                                "f:sshd_config": {}
                            },
                            "f:metadata": {
                                "f:annotations": {
                                    ".": {},
                                    "f:pg-port": {},
                                    "f:repo-path": {},
                                    "f:s3-bucket": {},
                                    "f:s3-endpoint": {},
                                    "f:s3-region": {},
                                    "f:s3-uri-style": {},
                                    "f:s3-verify-tls": {},
                                    "f:sshd-port": {},
                                    "f:supplemental-groups": {}
                                }
                            }
                        },
                        "manager": "postgres-operator",
                        "operation": "Update",
                        "time": "2020-11-25T09:27:31Z"
                    }
                ],
                "name": "postgres12-backrest-repo-config",
                "namespace": "playground-hoeggi",
                "resourceVersion": "94741462",
                "selfLink": "/api/v1/namespaces/playground-hoeggi/secrets/postgres12-backrest-repo-config",
                "uid": "8e6e2f27-c13c-4c16-93c7-85d5ebf7b6d4"
            },
            "type": "Opaque"
        },
        {
            "apiVersion": "v1",
            "data": {
                "password": "NXhoZHI7VCxEK0ZzNk5FfSltSjp7RnN3",
                "username": "cG9zdGdyZXM="
            },
            "kind": "Secret",
            "metadata": {
                "creationTimestamp": "2020-11-25T09:27:31Z",
                "labels": {
                    "pg-cluster": "postgres12",
                    "vendor": "crunchydata"
                },
                "managedFields": [
                    {
                        "apiVersion": "v1",
                        "fieldsType": "FieldsV1",
                        "fieldsV1": {
                            "f:data": {
                                ".": {},
                                "f:password": {},
                                "f:username": {}
                            },
                            "f:metadata": {
                                "f:labels": {
                                    ".": {},
                                    "f:pg-cluster": {},
                                    "f:vendor": {}
                                }
                            },
                            "f:type": {}
                        },
                        "manager": "apiserver",
                        "operation": "Update",
                        "time": "2020-11-25T09:27:31Z"
                    }
                ],
                "name": "postgres12-postgres-secret",
                "namespace": "playground-hoeggi",
                "resourceVersion": "94741443",
                "selfLink": "/api/v1/namespaces/playground-hoeggi/secrets/postgres12-postgres-secret",
                "uid": "e56ac50e-13b3-4db6-9340-ebb63bff9beb"
            },
            "type": "Opaque"
        },
        {
            "apiVersion": "v1",
            "data": {
                "password": "d3BieGVNaXQwQHd2XWp0XnZmTXRJR2xH",
                "username": "cHJpbWFyeXVzZXI="
            },
            "kind": "Secret",
            "metadata": {
                "creationTimestamp": "2020-11-25T09:27:31Z",
                "labels": {
                    "pg-cluster": "postgres12",
                    "vendor": "crunchydata"
                },
                "managedFields": [
                    {
                        "apiVersion": "v1",
                        "fieldsType": "FieldsV1",
                        "fieldsV1": {
                            "f:data": {
                                ".": {},
                                "f:password": {},
                                "f:username": {}
                            },
                            "f:metadata": {
                                "f:labels": {
                                    ".": {},
                                    "f:pg-cluster": {},
                                    "f:vendor": {}
                                }
                            },
                            "f:type": {}
                        },
                        "manager": "apiserver",
                        "operation": "Update",
                        "time": "2020-11-25T09:27:31Z"
                    }
                ],
                "name": "postgres12-primaryuser-secret",
                "namespace": "playground-hoeggi",
                "resourceVersion": "94741444",
                "selfLink": "/api/v1/namespaces/playground-hoeggi/secrets/postgres12-primaryuser-secret",
                "uid": "ab67b1cd-79e0-427c-abf1-6635b9659f16"
            },
            "type": "Opaque"
        },
        {
            "apiVersion": "v1",
            "data": {
                "password": "WUZlWD9iO1xYKVRfSWc/RVJIMzBecWk9",
                "username": "dGVzdHVzZXI="
            },
            "kind": "Secret",
            "metadata": {
                "creationTimestamp": "2020-11-25T09:27:31Z",
                "labels": {
                    "pg-cluster": "postgres12",
                    "vendor": "crunchydata"
                },
                "managedFields": [
                    {
                        "apiVersion": "v1",
                        "fieldsType": "FieldsV1",
                        "fieldsV1": {
                            "f:data": {
                                ".": {},
                                "f:password": {},
                                "f:username": {}
                            },
                            "f:metadata": {
                                "f:labels": {
                                    ".": {},
                                    "f:pg-cluster": {},
                                    "f:vendor": {}
                                }
                            },
                            "f:type": {}
                        },
                        "manager": "apiserver",
                        "operation": "Update",
                        "time": "2020-11-25T09:27:31Z"
                    }
                ],
                "name": "postgres12-testuser-secret",
                "namespace": "playground-hoeggi",
                "resourceVersion": "94741445",
                "selfLink": "/api/v1/namespaces/playground-hoeggi/secrets/postgres12-testuser-secret",
                "uid": "90191495-fa2a-47a0-9c19-9b0177c782a5"
            },
            "type": "Opaque"
        }
    ],
    "kind": "List",
    "metadata": {
        "resourceVersion": "",
        "selfLink": ""
    }
}
""".trimIndent()